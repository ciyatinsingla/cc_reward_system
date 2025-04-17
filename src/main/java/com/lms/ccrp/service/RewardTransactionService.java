package com.lms.ccrp.service;

import com.lms.ccrp.dto.RewardTransactionHistoryDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.RequestTransactions;
import com.lms.ccrp.entity.RewardTransactionHistory;
import com.lms.ccrp.enums.RequestType;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.RequestTransactionsRepository;
import com.lms.ccrp.repository.RewardTransactionRepository;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.util.EmailTemplateGenerator;
import com.lms.ccrp.util.RewardTransactionHistoryMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardTransactionService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final EmailTemplateGenerator emailTemplate;
    private final RequestTransactionsRepository requestTransactionsRepository;
    private final JwtService jwtService;
    private static final String APR = "Approved";

    /**
     * Parses an Excel file and converts each row (excluding the header) into a {@link RewardTransactionHistoryDTO}.
     * <p>
     * This method reads the first sheet of the uploaded Excel file and extracts the following fields from each row:
     * <ul>
     *     <li>Customer ID (Long)</li>
     *     <li>Name (String)</li>
     *     <li>Date of Birth (Date)</li>
     *     <li>Type of Request ({@link RequestType})</li>
     *     <li>Reward Description (String)</li>
     *     <li>Number of Points (int)</li>
     * </ul>
     * The first row is assumed to be a header and is skipped.
     *
     * @param file The Excel file (as {@link MultipartFile}) containing reward transaction data.
     * @return A list of {@link RewardTransactionHistoryDTO} parsed from the file.
     * @throws Exception If the file cannot be read or any cell data is invalid.
     */
    public List<RewardTransactionHistoryDTO> parseRTHFromExcelFile(MultipartFile file) throws Exception {
        List<RewardTransactionHistoryDTO> dtoList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                RewardTransactionHistoryDTO dto = new RewardTransactionHistoryDTO();
                dto.setCustomerId(getLongCellValue(row.getCell(0)));
                dto.setName(getStringCellValue(row.getCell(1)));
                dto.setDateOfBirth(row.getCell(2).getDateCellValue());
                dto.setTypeOfRequest(RequestType.fromLabel(getStringCellValue(row.getCell(3))));
                dto.setRewardDescription(getStringCellValue(row.getCell(4)));
                dto.setNumberOfPoints((long) row.getCell(5).getNumericCellValue());
                dto.setRequesterId(getStringCellValue(row.getCell(6)));
                dto.setTransactionTime(new Date());
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    /**
     * Processes and saves a list of reward point transactions for multiple customers.
     * <p>
     * For each {@link RewardTransactionHistoryDTO} in the provided list:
     * <ul>
     *     <li>Validates the existence of the customer using {@code customerId}</li>
     *     <li>Builds a {@link RewardTransactionHistory} entity from the DTO</li>
     *     <li>Sets the current timestamp as the transaction time</li>
     *     <li>Associates the transaction with the provided {@code requesterId}</li>
     * </ul>
     * The list of transactions is then saved in bulk using the {@code rewardTransactionRepository}.
     *
     * @param dtoList A list of {@link RewardTransactionHistoryDTO} containing transaction data to be processed.
     * @throws Exception If any customer referenced in the DTOs is not found in the system.
     */
    public void performRewardTransactions(List<RewardTransactionHistoryDTO> dtoList, String requesterId) throws Exception {
        List<RewardTransactionHistory> transactions = dtoList.stream()
                .filter(dto -> filterOutRepetitveEntries(dto))
                .map(dto -> {
                    Customer customer = customerRepository.findById(dto.getCustomerId())
                            .orElseThrow(() -> new RuntimeException("Customer not found: " + dto.getCustomerId()));

                    return RewardTransactionHistory.builder()
                            .customerId(dto.getCustomerId())
                            .name(dto.getName().trim())
                            .dateOfBirth(dto.getDateOfBirth())
                            .typeOfRequest(dto.getTypeOfRequest())
                            .rewardDescription(dto.getRewardDescription().trim())
                            .numberOfPoints(dto.getNumberOfPoints())
                            .transactionTime(new Date())
                            .requesterId(requesterId.trim())
                            .build();
                })
                .collect(Collectors.toList());


        rewardTransactionRepository.saveAll(transactions);

        requestEmailPORTemplateCreation(requesterId, transactions.get(0));
    }

    private boolean filterOutRepetitveEntries(RewardTransactionHistoryDTO dto) {
        List<RewardTransactionHistory> existing = rewardTransactionRepository.findByAllFields(
                dto.getCustomerId(),
                dto.getName().trim(),
                dto.getTypeOfRequest().toString().trim(),
                dto.getRewardDescription().trim(),
                dto.getNumberOfPoints(),
                dto.getRequesterId().trim());

        // Debug log
        if (!existing.isEmpty()) {
            System.out.println("Skipping existing record for customerId: " + dto.getCustomerId());
        }

        return existing.isEmpty(); // Keep only new records
    }

    /**
     * Extracts the user ID and role from the JWT token provided in the Authorization header.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Strips the "Bearer " prefix from the token</li>
     *     <li>Validates the JWT token</li>
     *     <li>Extracts the user ID from the token</li>
     *     <li>Fetches the user from the repository using the user ID</li>
     *     <li>Returns a map containing the user's role name as the key and user ID as the value</li>
     * </ul>
     *
     * @param authHeader The Authorization header containing the JWT token in the format "Bearer &lt;token&gt;"
     * @return A map with the user's role name as the key and the user ID as the value, or {@code null} if the token is
     * invalid or user ID is 0
     * @throws Exception if the user is not found in the repository
     */
    public Map<String, Long> getRequesterId(String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtService.validateToken(token))
            return null;
        Long userId = jwtService.extractUserId(token);
        if (userId == 0)
            return null;

        return Map.of(
                userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User Not Found."))
                        .getRole().name(),
                userId
        );
    }

    /**
     * Retrieves the total reward points for a specific customer.
     *
     * @param customerId The ID of the customer whose points are to be retrieved.
     * @return The total number of reward points for the customer.
     * @throws RuntimeException If the customer with the given ID is not found.
     */
    public Long getCustomerPoints(Long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found: " + customerId)).getTotalPoints();
    }

    /**
     * Retrieves all reward point transactions for a specific customer.
     *
     * @param customerId The ID of the customer whose transaction history is to be retrieved.
     * @return A list of {@link RewardTransactionHistoryDTO} records associated with the customer.
     */
    public List<RewardTransactionHistoryDTO> getCustomerTransactions(Long customerId) {
        List<RewardTransactionHistoryDTO> dtoList = new ArrayList<>();
        List<RewardTransactionHistory> history = rewardTransactionRepository.findByCustomerId(customerId);
        for (RewardTransactionHistory rewardTransactionHistory : history) {
            dtoList.add(RewardTransactionHistoryMapper.entityToRewardTransactionHistoryDTO(rewardTransactionHistory));
        }
        return dtoList;
    }

    /**
     * Triggers the creation of a POI-based email template for a given reward transaction,
     * only if the requester is identified as a user.
     *
     * <p>This method checks whether the requester ID starts with the {@code Role.USER} prefix.
     * If the check passes, it delegates the creation of the email template to the
     * {@code emailTemplate.createPOIRTemplate()} method using the provided transaction details.</p>
     *
     * @param requesterId the ID of the user or system initiating the request; expected to be prefixed with role information.
     * @param transaction the reward transaction data used to populate the email template.
     */
    private void requestEmailPORTemplateCreation(String requesterId, RewardTransactionHistory transaction) throws ParseException {
        if (requesterId.startsWith(Role.USER.toString()))
            emailTemplate.createPOIRTemplate(transaction);
    }

    /**
     * Safely extracts a trimmed string value from an Excel cell.
     *
     * @param cell The Excel {@link Cell} from which to extract the value.
     * @return The string value of the cell, or {@code null} if the cell is {@code null}.
     */
    private static String getStringCellValue(Cell cell) {
        return cell == null ? null : cell.toString().trim();
    }

    /**
     * Safely extracts a long value from a numeric Excel cell.
     *
     * @param cell The Excel {@link Cell} from which to extract the value.
     * @return The long value of the cell, or {@code null} if the cell is {@code null}.
     */
    private static Long getLongCellValue(Cell cell) {
        return cell == null ? null : (long) cell.getNumericCellValue();
    }

    /**
     * Reads reward transaction data from the first sheet of the given Excel file
     * and maps each row (after the header) to a {@link RequestTransactions} object.
     * <p>
     * The expected columns in each row are:
     * <ol>
     *   <li>Customer ID (numeric)</li>
     *   <li>Name (string)</li>
     *   <li>Date of Birth (date)</li>
     *   <li>Request Type (string; mapped via {@link RequestType#fromLabel(String)})</li>
     *   <li>Reward Description (string)</li>
     *   <li>Number of Points (numeric)</li>
     *   <li>Requester ID (string)</li>
     *   <li>Request Status (string)</li>
     *   <li>Reason (string)</li>
     * </ol>
     * The {@code transactionTime} field of each transaction is set to the current date/time
     * at the moment of reading.
     *
     * @param excelFilePath the file system path to the .xlsx file to read
     * @return a {@link List} of {@link RequestTransactions} objects populated from the Excel rows;
     * returns an empty list if no data rows are found
     * @throws RuntimeException if an I/O error occurs while opening the file,
     *                          if the file format is invalid, or if any other exception
     *                          arises during reading/mapping
     */
    public List<RequestTransactions> parseSRTFromExcelFile(String excelFilePath) {
        List<RequestTransactions> transactions = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                RequestTransactions transaction = new RequestTransactions();
                transaction.setCustomerId(getLongCellValue(row.getCell(0)));
                transaction.setName(getStringCellValue(row.getCell(1)));
                transaction.setDateOfBirth(row.getCell(2).getDateCellValue());
                transaction.setTypeOfRequest(RequestType.fromLabel(getStringCellValue(row.getCell(3))));
                transaction.setRewardDescription(getStringCellValue(row.getCell(4)));
                transaction.setNumberOfPoints((long) row.getCell(5).getNumericCellValue());
                transaction.setRequesterId(getStringCellValue(row.getCell(6)));
                transaction.setTransactionTime(new Date());
                transaction.setRequestStatus(row.getCell(7).getStringCellValue());
                transaction.setReason(row.getCell(8).getStringCellValue());

                transactions.add(transaction);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while parcel Source Transactions.");
        }
        return transactions;
    }

    @Transactional
    public void requestTransactions(@NonNull List<RequestTransactions> requestTransactionsList) throws Exception {
        for (RequestTransactions request : requestTransactionsList) {
            List<RequestTransactions> existingRequests = requestTransactionsRepository.findByAllFields(request.getCustomerId(), request.getName(), request.getTypeOfRequest().toString(), request.getRewardDescription(), request.getNumberOfPoints(), request.getRequesterId(), request.getRequestStatus(), request.getReason());
            if (!existingRequests.isEmpty()) continue;
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));
            boolean isSuccess = request.getRequestStatus().trim().equalsIgnoreCase(APR);
            Long availablePoints = customer.getTotalPoints();
            Long transactionPoints = Math.abs(request.getNumberOfPoints());
            if (request.getTypeOfRequest() != RequestType.EARNED) {
                if (availablePoints < transactionPoints)
                    throw new RuntimeException("");
                if (isSuccess) customer.setTotalPoints(availablePoints - transactionPoints);
                request.setNumberOfPoints(-transactionPoints);
            } else {
                if (isSuccess) customer.setTotalPoints(availablePoints + transactionPoints);
                request.setNumberOfPoints(transactionPoints);
            }

            RequestTransactions finalTransaction = RequestTransactions.builder()
                    .customerId(request.getCustomerId())
                    .name(request.getName())
                    .dateOfBirth(request.getDateOfBirth())
                    .typeOfRequest(request.getTypeOfRequest())
                    .rewardDescription(request.getRewardDescription())
                    .numberOfPoints(request.getNumberOfPoints())
                    .transactionTime(new Date())
                    .requesterId(request.getRequesterId())
                    .requestStatus(request.getRequestStatus())
                    .reason(request.getReason())
                    .build();

            if (isSuccess)
                finalTransaction.setCompleted();

            if (isSuccess) customerRepository.save(customer);
            requestTransactionsRepository.save(finalTransaction);
        }
    }
}