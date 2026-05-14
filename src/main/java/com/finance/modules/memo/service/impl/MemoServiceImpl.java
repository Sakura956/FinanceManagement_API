package com.finance.modules.memo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.exception.BusinessException;
import com.finance.common.result.PageResult;
import com.finance.modules.memo.dto.MemoRequest;
import com.finance.modules.memo.dto.MemoVO;
import com.finance.modules.memo.entity.Memo;
import com.finance.modules.memo.mapper.MemoMapper;
import com.finance.modules.memo.service.MemoService;
import com.finance.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemoServiceImpl implements MemoService {

    private final MemoMapper memoMapper;

    public MemoServiceImpl(MemoMapper memoMapper) {
        this.memoMapper = memoMapper;
    }

    /**
     * 把数据库实体 Memo → 转换成前端需要的 MemoVO
     * @param memo
     * @return
     */
    private MemoVO toMemoVO(Memo memo) {
        MemoVO vo = new MemoVO();
        vo.setId(memo.getId());
        vo.setTitle(memo.getTitle());
        vo.setContent(memo.getContent());
        vo.setRemindTime(memo.getRemindTime() != null ? memo.getRemindTime().toString() : null);
        vo.setIsCompleted(memo.getIsCompleted());
        vo.setCreateTime(memo.getCreateTime() != null ? memo.getCreateTime().toString() : null);
        vo.setUpdateTime(memo.getUpdateTime() != null ? memo.getUpdateTime().toString() : null);
        return vo;
    }

    /**
     * 分页查询备忘录（列表）
     * @param isCompleted
     * @param page
     * @param size
     * @param keyword
     * @return
     */
    @Override
    public PageResult<?> listMemos(Integer isCompleted, int page, int size, String keyword) {
        Long userId = SecurityUtil.getCurrentUserId();
        LambdaQueryWrapper<Memo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Memo::getUserId, userId);
        if (isCompleted != null) {
            wrapper.eq(Memo::getIsCompleted, isCompleted);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Memo::getTitle, keyword);
        }
        wrapper.orderByDesc(Memo::getCreateTime);

        //分页查询
        Page<Memo> result = memoMapper.selectPage(new Page<>(page, size), wrapper);

        List<MemoVO> records = new ArrayList<>();
        for (Memo memo : result.getRecords()) {
            records.add(toMemoVO(memo));
        }

        return new PageResult<>(result.getTotal(), page, size, records);
    }

    /**
     * 获取备忘录详情
     * @param id
     * @return
     */
    @Override
    public Object getMemoDetail(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Memo memo = memoMapper.selectOne(
                new LambdaQueryWrapper<Memo>()
                        .eq(Memo::getId, id)
                        .eq(Memo::getUserId, userId));
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        return toMemoVO(memo);
    }

    /**
     * 创建备忘录
     * @param request
     * @return
     */
    @Override
    public Object createMemo(MemoRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setTitle(request.getTitle());
        memo.setContent(request.getContent());
        if (StringUtils.hasText(request.getRemindTime())) {
            memo.setRemindTime(LocalDateTime.parse(request.getRemindTime(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        memo.setIsCompleted(0);
        memoMapper.insert(memo);
        return toMemoVO(memo);
    }

    /**
     * 修改备忘录
     * @param id
     * @param request
     * @return
     */
    @Override
    public Object updateMemo(Long id, MemoRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Memo memo = memoMapper.selectOne(
                new LambdaQueryWrapper<Memo>()
                        .eq(Memo::getId, id)
                        .eq(Memo::getUserId, userId));
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        if (StringUtils.hasText(request.getTitle())) {
            memo.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            memo.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getRemindTime())) {
            memo.setRemindTime(LocalDateTime.parse(request.getRemindTime(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        memoMapper.updateById(memo);
        return toMemoVO(memo);
    }

    /**
     * 切换完成状态（0 ↔ 1）
     * @param id
     * @return
     */
    @Override
    public Object toggleComplete(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Memo memo = memoMapper.selectOne(
                new LambdaQueryWrapper<Memo>()
                        .eq(Memo::getId, id)
                        .eq(Memo::getUserId, userId));
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        memo.setIsCompleted(memo.getIsCompleted() != null && memo.getIsCompleted() == 1 ? 0 : 1);
        memoMapper.updateById(memo);

        Map<String, Object> result = new HashMap<>();
        result.put("id", memo.getId());
        result.put("isCompleted", memo.getIsCompleted());
        result.put("updateTime", memo.getUpdateTime() != null ? memo.getUpdateTime().toString() : null);
        return result;
    }

    /**
     * 删除备忘录
     * @param id
     */
    @Override
    public void deleteMemo(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Memo memo = memoMapper.selectOne(
                new LambdaQueryWrapper<Memo>()
                        .eq(Memo::getId, id)
                        .eq(Memo::getUserId, userId));
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        memoMapper.deleteById(id);
    }
}
