package com.pharmacy.pharmacy_management.service;

import com.pharmacy.pharmacy_management.dto.SaleRequestDTO;
import com.pharmacy.pharmacy_management.dto.SaleResponseDTO;
import com.pharmacy.pharmacy_management.entity.Sale;
import com.pharmacy.pharmacy_management.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private MedicineRepository medicineRepository;
    
    @Mock
    private GlassesRepository glassesRepository;
    
    @Mock
    private GlassesAccessoryRepository glassesAccessoryRepository;
    
    @Mock
    private GlassesRepairRepository glassesRepairRepository;
    
    @Mock
    private SurgeryRepository surgeryRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void testCreateSaleWithDiscount() {
        // Given
        SaleRequestDTO request = SaleRequestDTO.builder()
                .customerName("John Doe")
                .customerPhone("+2348012345678")
                .paymentMethod("CASH")
                .discountType("PERCENT")
                .discountValue(BigDecimal.valueOf(10))
                .discountAmount(BigDecimal.valueOf(100))
                .items(List.of(
                        new SaleRequestDTO.SaleItemInput("CLINIC_VISIT", null, 1, "Consultation", BigDecimal.valueOf(1000))
                ))
                .build();

        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        // When
        SaleResponseDTO response = saleService.createSale(request);

        // Then
        assertNotNull(response);
        assertEquals("PERCENT", response.getDiscountType());
        assertEquals(0, response.getDiscountValue().compareTo(BigDecimal.valueOf(10)));
        assertEquals(0, response.getDiscountAmount().compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, response.getGrandTotal().compareTo(BigDecimal.valueOf(900))); // 1000 - 100
    }
}
