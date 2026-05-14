package com.finance.modules.memo.controller;

import com.finance.common.result.Result;
import com.finance.modules.memo.dto.MemoRequest;
import com.finance.modules.memo.service.MemoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/memos")
public class MemoController {

    private final MemoService memoService;
    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }


    /**
     * 备忘录列表
     * @param isCompleted
     * @param page
     * @param size
     * @param keyword
     * @return
     */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) Integer isCompleted,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(memoService.listMemos(isCompleted, page, size, keyword));
    }


    /**
     * 备忘录详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(memoService.getMemoDetail(id));
    }


    /**
     * 新增备忘录
     * @param request
     * @return
     */
    @PostMapping
    public Result<?> create(@RequestBody MemoRequest request) {
        return Result.success("备忘录创建成功", memoService.createMemo(request));
    }


    /**
     * 修改备忘录
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody MemoRequest request) {
        return Result.success("备忘录更新成功", memoService.updateMemo(id, request));
    }


    /**
     * 完成/取消完成备忘录
     * @param id
     * @return
     */
    @PutMapping("/{id}/toggle")
    public Result<?> toggle(@PathVariable Long id) {
        return Result.success("状态切换成功", memoService.toggleComplete(id));
    }


    /**
     * 删除备忘录
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memoService.deleteMemo(id);
        return Result.success("备忘录删除成功", null);
    }
}
