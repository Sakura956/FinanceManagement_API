package com.finance.modules.bill.controller;

import com.finance.common.result.Result;
import com.finance.modules.bill.dto.BillCreateRequest;
import com.finance.modules.bill.dto.BillUpdateRequest;
import com.finance.modules.bill.service.BillService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    /**
     * 记账
     *
     * @param request
     * @return
     */
    @PostMapping
    public Result<?> create(@Valid @RequestBody BillCreateRequest request) {
        return Result.success("记账成功", billService.createBill(request));
    }

    /**
     * 账单分页查询，支持多条件筛选
     *
     * @param page
     * @param size
     * @param type
     * @param categoryId
     * @param startDate
     * @param endDate
     * @param keyword
     * @param minAmount
     * @param maxAmount
     * @param sortBy
     * @param order
     * @return
     */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer type,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Double minAmount,
                          @RequestParam(required = false) Double maxAmount,
                          @RequestParam(defaultValue = "recordTime") String sortBy,
                          @RequestParam(defaultValue = "desc") String order) {

        return Result.success(billService.listBills(page, size, type, categoryId,
                startDate, endDate, keyword, minAmount, maxAmount, sortBy, order));
    }

    /**
     * 账单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(billService.getBillDetail(id));
    }


    /**
     * 修改账单
     *
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody BillUpdateRequest request) {
        return Result.success("账单修改成功", billService.updateBill(id, request));
    }


    /**
     * 删除账单
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        billService.deleteBill(id);
        return Result.success("账单删除成功", null);
    }


    /**
     * 批量删除账单
     *
     * @param body
     * @return
     */
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody Map<String, List<Long>> body) {
        //Map<String, List<Long>>代表key 是 String，value 是 Long 类型的列表
        int deletedCount = billService.batchDeleteBills(body.get("ids"));
        return Result.success("删除成功，共处理 " + deletedCount + " 条",
                Map.of("deletedCount", deletedCount));
    }
}
