package com.finance.modules.memo.service;

import com.finance.common.result.PageResult;
import com.finance.modules.memo.dto.MemoRequest;

public interface MemoService {

    PageResult<?> listMemos(Integer isCompleted, int page, int size, String keyword);

    Object getMemoDetail(Long id);

    Object createMemo(MemoRequest request);

    Object updateMemo(Long id, MemoRequest request);

    Object toggleComplete(Long id);

    void deleteMemo(Long id);
}
