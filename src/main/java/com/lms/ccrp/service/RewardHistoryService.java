package com.lms.ccrp.service;

import com.lms.ccrp.dto.RewardHistoryDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.RewardHistory;
import com.lms.ccrp.entity.SourceTransactions;
import com.lms.ccrp.enums.RequestStatus;
import com.lms.ccrp.enums.RewardRequestType;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.RewardHistoryRepository;
import com.lms.ccrp.repository.SourceTransactionsRepository;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.util.RewardTransactionHistoryMapper;
import com.lms.ccrp.util.RewardUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class RewardHistoryService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RewardHistoryRepository rewardHistoryRepository;
    private final SourceTransactionsRepository sourceTransactionsRepository;
    private final RewardUtils rewardUtils;
    private final JwtService jwtService;

    @Value("${source.file.path}")
    private String source;

    /**
     * Parses an Excel file and converts each row (excluding the header) into a {@link RewardHistoryDTO}.
     * <p>
     * This method reads the first sheet of the uploaded Excel file and extracts the following fields from each row:
     * <ul>
     *     <li>Customer ID (Long)</li>
     *     <li>Name (String)</li>
     *     <li>Date of Birth (Date)</li>
     *     <li>Type of Request ({@link RewardRequestType})</li>
     *     <li>Reward Description (String)</li>
     *     <li>Number of Points (int)</li>
     * </ul>
     * The first row is assumed to be a header and is skipped.
     *
     * @param file The Excel file (as {@link MultipartFile}) containing reward transaction data.
     * @return A list of {@link RewardHistoryDTO} parsed from the file.
     * @throws Exception If the file cannot be read or any cell data is invalid.
     */
    public List<RewardHistoryDTO> parseRTHFromExcelFile(MultipartFile file) throws Exception {
        List<RewardHistoryDTO> dtoList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                RewardHistoryDTO dto = new RewardHistoryDTO();
                dto.setCustomerId(getLongCellValue(row.getCell(0)));
                dto.setName(getStringCellValue(row.getCell(1)));
                dto.setDateOfBirth(row.getCell(2).getDateCellValue());
                dto.setTypeOfRequest(RewardRequestType.fromLabel(getStringCellValue(row.getCell(3))));
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
     * For each {@link RewardHistoryDTO} in the provided list:
     * <ul>
     *     <li>Validates the existence of the customer using {@code customerId}</li>
     *     <li>Builds a {@link RewardHistory} entity from the DTO</li>
     *     <li>Sets the current timestamp as the transaction time</li>
     *     <li>Associates the transaction with the provided {@code requesterId}</li>
     * </ul>
     * The list of transactions is then saved in bulk using the {@code rewardTransactionRepository}.
     *
     * @param dtoList A list of {@link RewardHistoryDTO} containing transaction data to be processed.
     * @throws Exception If any customer referenced in the DTOs is not found in the system.
     */
    public void performRewardTransactions(List<RewardHistoryDTO> dtoList) throws Exception {
        List<RewardHistory> transactions = dtoList.stream()
                .map(dto -> {
                    Customer customer = customerRepository.findById(dto.getCustomerId())
                            .orElseThrow(() -> new RuntimeException("Customer not found: " + dto.getCustomerId()));
                    if (!RewardRequestType.EARNED.equals(dto.getTypeOfRequest()))
                        dto.setNumberOfPoints(-dto.getNumberOfPoints());
                    return RewardHistory.builder()
                            .customerId(dto.getCustomerId())
                            .name(dto.getName().trim())
                            .dateOfBirth(customer.getDateOfBirth())
                            .typeOfRequest(dto.getTypeOfRequest())
                            .rewardDescription(dto.getRewardDescription().trim())
                            .numberOfPoints(dto.getNumberOfPoints())
                            .transactionTime(new Date())
                            .requesterId(dto.getRequesterId())
                            .build();
                })
                .collect(Collectors.toList());

        rewardHistoryRepository.saveAll(transactions);
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
     * @return A list of {@link RewardHistoryDTO} records associated with the customer.
     */
    public List<RewardHistoryDTO> getCustomerTransactions(Long customerId) {
        List<RewardHistoryDTO> dtoList = new ArrayList<>();
        List<RewardHistory> history = rewardHistoryRepository.findByCustomerId(customerId);
        for (RewardHistory rewardHistory : history) {
            dtoList.add(RewardTransactionHistoryMapper.entityToRewardTransactionHistoryDTO(rewardHistory));
        }
        return dtoList;
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
     * and maps each row (after the header) to a {@link SourceTransactions} object.
     * <p>
     * The expected columns in each row are:
     * <ol>
     *   <li>Customer ID (numeric)</li>
     *   <li>Name (string)</li>
     *   <li>Date of Birth (date)</li>
     *   <li>Request Type (string; mapped via {@link RewardRequestType#fromLabel(String)})</li>
     *   <li>Reward Description (string)</li>
     *   <li>Number of Points (numeric)</li>
     *   <li>Requester ID (string)</li>
     *   <li>Request Status (string)</li>
     *   <li>Reason (string)</li>
     * </ol>
     * The {@code transactionTime} field of each transaction is set to the current date/time
     * at the moment of reading.
     *
     * @return a {@link List} of {@link SourceTransactions} objects populated from the Excel rows;
     * returns an empty list if no data rows are found
     * @throws RuntimeException if an I/O error occurs while opening the file,
     *                          if the file format is invalid, or if any other exception
     *                          arises during reading/mapping
     */
    public List<SourceTransactions> parseSRTExcelFile() {
        List<SourceTransactions> transactions = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (new File(source).exists()) {
            try (FileInputStream fis = new FileInputStream(new File(source));
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                boolean isHeader = true;

                for (Row row : sheet) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    SourceTransactions transaction = new SourceTransactions();
                    transaction.setCustomerId(getLongCellValue(row.getCell(0)));
                    transaction.setName(getStringCellValue(row.getCell(1)));
                    String date = row.getCell(2).getStringCellValue();
                    transaction.setDateOfBirth(sdf.parse(date));
                    transaction.setTypeOfRequest(RewardRequestType.fromLabel(getStringCellValue(row.getCell(3))));
                    transaction.setRewardDescription(getStringCellValue(row.getCell(4)));
                    transaction.setNumberOfPoints((long) row.getCell(5).getNumericCellValue());
                    transaction.setRequesterId(getStringCellValue(row.getCell(6)));
                    transaction.setTransactionTime(new Date());
                    transactions.add(transaction);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error occurred while parsing Source Transactions.");
            }
        }
        return transactions;
    }

    @Transactional
    public void requestTransactions(@NonNull List<SourceTransactions> sourceTransactionsList) throws Exception {
        for (SourceTransactions request : sourceTransactionsList) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));
            Long availablePoints = customer.getTotalPoints();
            Long transactionPoints = Math.abs(request.getNumberOfPoints());
            RewardRequestType transactionRequestType = request.getTypeOfRequest();
            boolean isSuccess = true;
            if (RewardRequestType.EARNED.equals(transactionRequestType)) {
                customer.setTotalPoints(availablePoints + transactionPoints);
                request.setRequestStatus(RequestStatus.APPROVED.getLabel());
                request.setReason("Return policy ended");
            } else {
                if (availablePoints < transactionPoints) {
                    isSuccess = false;
                    request.setRequestStatus(RequestStatus.REJECTED.getLabel());
                    request.setReason(RequestStatus.POINTS_UNAVAILABLE.getLabel());
                    log.info("Customer balance is low. Hence, declining the transaction of {}", transactionRequestType);
                } else {
                    request.setRequestStatus(RequestStatus.APPROVED.getLabel());
                    customer.setTotalPoints(availablePoints - transactionPoints);
                    request.setReason(RequestStatus.PRODUCT_AVAILABLE.getLabel());
                    if (RewardRequestType.EXPIRED.equals(transactionRequestType))
                        request.setReason(RewardRequestType.EXPIRED_MESSAGE.getLabel());
                }
            }
            request.setNumberOfPoints(transactionPoints);
            SourceTransactions finalTransaction = SourceTransactions.builder()
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
            sourceTransactionsRepository.save(finalTransaction);
        }
        rewardUtils.notifySourceSystem(source);
    }

    /**
     * Finalizes and records all reward history transactions for the current date for source.
     * <p>
     * This method performs the following:
     * <ul>
     *     <li>Fetches all transactions from the repository for today's date.</li>
     *     <li>Creates an Excel workbook with a formatted header row and transaction data.</li>
     *     <li>Saves the workbook to a file specified by the {@code source} path.</li>
     * </ul>
     * The generated Excel file contains columns like customer ID, name, date of birth, request type,
     * reward description, points, requester ID, request status, and reason.
     *
     * @throws IOException if an I/O error occurs while writing the Excel file.
     */
    public void finalizeSyncHistory() throws IOException {
        LocalDate localDate = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        Date startOfDay = Date.from(localDate.atStartOfDay(zone).toInstant());
        Date endOfDay = Date.from(localDate.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant());
        List<RewardHistory> rewardHistoryTransactions = rewardHistoryRepository.findAllTransactionsForDate(startOfDay, endOfDay);
        String[] headers = {
                "customer_id", "name", "date_of_birth", "type_of_request",
                "reward_description", "number_of_points", "requester_id",
                "request_status", "reason"
        };
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PointsHistory");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (RewardHistory rewardHistory : rewardHistoryTransactions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rewardHistory.getCustomerId());
            row.createCell(1).setCellValue(rewardHistory.getName());
            row.createCell(2).setCellValue(sdf.format(rewardHistory.getDateOfBirth()));
            row.createCell(3).setCellValue(RewardRequestType.fromRewardRequestType(rewardHistory.getTypeOfRequest()));
            row.createCell(4).setCellValue(rewardHistory.getRewardDescription());
            row.createCell(5).setCellValue(rewardHistory.getNumberOfPoints());
            row.createCell(6).setCellValue(rewardHistory.getRequesterId());
        }

        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);

        try (FileOutputStream fileOut = new FileOutputStream(source)) {
            workbook.write(fileOut);
            workbook.close();
        } catch (IOException e) {
            log.error("Error occurred while recording Source Transactions.");
        } finally {
            workbook.close();
        }
    }
}