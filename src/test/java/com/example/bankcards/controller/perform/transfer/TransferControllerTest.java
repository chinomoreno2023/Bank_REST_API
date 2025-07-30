package com.example.bankcards.controller.perform.transfer;

import com.example.bankcards.config.security.EncryptionProperties;
import com.example.bankcards.dto.perform.OperationType;
import com.example.bankcards.dto.perform.transfer.TransferRequest;
import com.example.bankcards.security.auth.JwtAuthFilter;
import com.example.bankcards.security.auth.JwtService;
import com.example.bankcards.service.perform.PerformService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {
    private static final String URL = "/transfer";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PerformService<TransferRequest> transferService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private EncryptionProperties encryptionProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void perform_shouldReturnNoContent_whenRequestIsValid() throws Exception {
        TransferRequest request = buildValidRequest();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isNoContent(),
                        content().string("")
                );

        Mockito.verify(transferService).perform(ArgumentMatchers.refEq(request));
    }

    @Test
    void perform_shouldReturnBadRequest_whenAmountIsNull() throws Exception {
        TransferRequest invalidRequest = buildValidRequest();
        invalidRequest.setAmount(null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(transferService, Mockito.never()).perform(ArgumentMatchers.any(TransferRequest.class));
    }

    @Test
    void perform_shouldReturnBadRequest_whenAmountIsTooSmall() throws Exception {
        TransferRequest invalidRequest = buildValidRequest();
        invalidRequest.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(transferService, Mockito.never()).perform(ArgumentMatchers.any(TransferRequest.class));
    }

    @Test
    void perform_shouldReturnBadRequest_whenAmountIsNegative() throws Exception {
        TransferRequest invalidRequest = buildValidRequest();
        invalidRequest.setAmount(BigDecimal.valueOf(-1));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(transferService, Mockito.never()).perform(ArgumentMatchers.any(TransferRequest.class));
    }

    @Test
    void perform_shouldReturnBadRequest_whenOperationTypeIsNull() throws Exception {
        TransferRequest invalidRequest = buildValidRequest();
        invalidRequest.setOperationType(null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(transferService, Mockito.never()).perform(ArgumentMatchers.any(TransferRequest.class));
    }

    @Test
    void perform_shouldReturnBadRequest_whenFromCardIdIsNull() throws Exception {
        TransferRequest request = buildValidRequest();
        request.setFromCardId(null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        Mockito.verify(transferService,Mockito.never()).perform(ArgumentMatchers.refEq(request));
    }

    @Test
    void perform_shouldReturnBadRequest_whenToCardIdIsNull() throws Exception {
        TransferRequest request = buildValidRequest();
        request.setToCardId(null);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        Mockito.verify(transferService,Mockito.never()).perform(ArgumentMatchers.refEq(request));
    }

    private TransferRequest buildValidRequest() {
        return TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("100.00"))
                .operationType(OperationType.TRANSFER)
                .build();
    }
}
