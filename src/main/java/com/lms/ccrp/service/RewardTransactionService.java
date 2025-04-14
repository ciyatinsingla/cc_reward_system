package com.lms.ccrp.service;

import com.lms.ccrp.dto.RewardTransactionHistoryDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.RewardTransactionHistory;
import com.lms.ccrp.enums.RequestType;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.RewardTransactionRepository;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.util.RewardTransactionHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RewardTransactionService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final JwtService jwtService;

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
    public List<RewardTransactionHistoryDTO> parseExcelFile(MultipartFile file) throws Exception {
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
                dto.setTypeOfRequest(RequestType.valueOf(getStringCellValue(row.getCell(3))));
                dto.setRewardDescription(getStringCellValue(row.getCell(4)));
                dto.setNumberOfPoints((int) row.getCell(5).getNumericCellValue());

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
     * @param dtoList     A list of {@link RewardTransactionHistoryDTO} containing transaction data to be processed.
     * @param requesterId The ID of the user (e.g., admin) performing the transaction operation.
     * @throws Exception If any customer referenced in the DTOs is not found in the system.
     */
    public void performTransactions(List<RewardTransactionHistoryDTO> dtoList, String requesterId) throws Exception {
        List<RewardTransactionHistory> transactions = dtoList.stream().map(dto -> {
            Customer customer = customerRepository.findByUserId(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + dto.getCustomerId()));

            return RewardTransactionHistory.builder()
                    .customerId(dto.getCustomerId())
                    .name(dto.getName())
                    .dateOfBirth(dto.getDateOfBirth())
                    .typeOfRequest(dto.getTypeOfRequest())
                    .rewardDescription(dto.getRewardDescription())
                    .numberOfPoints(dto.getNumberOfPoints())
                    .transactionTime(new Date())
                    .requesterId(requesterId)
                    .build();
        }).toList();

        rewardTransactionRepository.saveAll(transactions);
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
    public int getCustomerPoints(Long customerId) {
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
     * Safely extracts a trimmed string value from an Excel cell.
     *
     * @param cell The Excel {@link Cell} from which to extract the value.
     * @return The string value of the cell, or {@code null} if the cell is {@code null}.
     */
    private String getStringCellValue(Cell cell) {
        return cell == null ? null : cell.toString().trim();
    }

    /**
     * Safely extracts a long value from a numeric Excel cell.
     *
     * @param cell The Excel {@link Cell} from which to extract the value.
     * @return The long value of the cell, or {@code null} if the cell is {@code null}.
     */
    private Long getLongCellValue(Cell cell) {
        return cell == null ? null : (long) cell.getNumericCellValue();
    }
}
