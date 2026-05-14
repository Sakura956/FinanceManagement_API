package com.finance.modules.bill.service;

import com.finance.common.result.PageResult;
import com.finance.modules.bill.dto.BillCreateRequest;
import com.finance.modules.bill.dto.BillUpdateRequest;

import java.util.List;

public interface BillService {

    Object createBill(BillCreateRequest request);

    Object listBills(int page, int size, Integer type, Long categoryId,
                     String startDate, String endDate, String keyword,
                     Double minAmount, Double maxAmount, String sortBy, String order);

    Object getBillDetail(Long id);

    Object updateBill(Long id, BillUpdateRequest request);

    void deleteBill(Long id);

    int batchDeleteBills(List<Long> ids);
}
