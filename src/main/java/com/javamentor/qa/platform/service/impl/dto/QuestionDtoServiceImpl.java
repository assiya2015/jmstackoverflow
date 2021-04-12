package com.javamentor.qa.platform.service.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.QuestionDtoDao;
import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import com.javamentor.qa.platform.service.abstracts.dto.QuestionDtoService;
import com.javamentor.qa.platform.service.impl.dto.pagination.question.PaginationQuestionDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuestionDtoServiceImpl extends PaginationQuestionDtoService implements QuestionDtoService{

    private final QuestionDtoDao questionDtoDao;

    @Autowired
    public QuestionDtoServiceImpl(QuestionDtoDao questionDtoDao) {
        this.questionDtoDao = questionDtoDao;
    }

    @Transactional
    public Optional<QuestionDto> getQuestionDtoById(Long id) {
        return questionDtoDao.getQuestionDtoById(id);
    }

    public PageDto<QuestionDto, Object> getPagination(int page, int size) {
        return getPageDto(
                "paginationQuestion",
                setPaginationParameters(page, size, Optional.empty(), Optional.empty()));
    }

    public PageDto<QuestionDto, Object> getPaginationPopular(int page, int size, long days) {
        Map<String, Object> parameters = setPaginationParameters(page, size, Optional.empty(), Optional.empty());
        parameters.put("days", days);
        return getPageDto("paginationQuestionByPopular", parameters);
    }

    public PageDto<QuestionDto, Object> getPaginationOrderedNew(int page, int size) {
        return getPageDto(
                "paginationQuestionOrderByNew",
                setPaginationParameters(page, size, Optional.empty(), Optional.empty()));
    }

    public PageDto<QuestionDto, Object> getPaginationWithoutAnswers(int page, int size) {
        return getPageDto(
                "paginationQuestionWithoutAnswers",
                setPaginationParameters(page, size, Optional.empty(), Optional.empty()));
    }

    public PageDto<QuestionDto, Object> getPaginationWithGivenTags(int page, int size, List<Long> tagIds) {
        return getPageDto(
                "paginationQuestionWithGivenTags",
                setPaginationParameters(page, size, Optional.ofNullable(tagIds), Optional.empty()));
    }

    public PageDto<QuestionDto, Object> getPaginationWithoutTags(int page, int size, List<Long> tagIds) {
        return getPageDto(
                "paginationQuestionWithoutTags",
                setPaginationParameters(page, size, Optional.ofNullable(tagIds), Optional.empty()));
    }

    @Override
    public PageDto<QuestionDto, Object> getQuestionBySearchValue(String message, int page, int size) {
        return getPageDto(
                "paginationQuestionBySearchValue",
                setPaginationParameters(page, size, Optional.empty(), Optional.ofNullable(message)));
    }

    @Override
    public PageDto<QuestionDto, Object> getPaginationWithoutAnswersNew(int page, int size){
        List<Long> ids = questionDtoDao.getPaginationQuestionIdsWithoutAnswerOrderByNew(page, size);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page);
        parameters.put("size", size);
        parameters.put("ids", ids);

        return getPageDto(
                "paginationQuestionWithoutAnswersNew",
                parameters);
    }

    @Override
    public PageDto<QuestionDto, Object> getPaginationWithoutAnswersTrackedTag(int page, int size, long id){
        List<Long> ids = questionDtoDao.getPaginationQuestionIdsWithoutAnswerWithTrackedTags(page, size, id);
        Map<String, Object> parameters = setPaginationParameters(page, size, Optional.empty(), Optional.empty());
        parameters.put("page", page);
        parameters.put("size", size);
        parameters.put("ids", ids);
        parameters.put("id", id);
        return getPageDto(
                "paginationWithoutAnswersTrackedTag",
                parameters);
    }

    private Map<String, Object> setPaginationParameters(int page, int size, Optional<List<Long>> tagsIds, Optional<String> message) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page);
        parameters.put("size", size);
        parameters.put("tagsIds", tagsIds.orElse(new ArrayList<>()));
        parameters.put("message", message.orElse(""));
        return parameters;
    }

    @Override
    public PageDto<QuestionDto, Object> getPaginationWithoutAnswersIgnoredTags(int page, int size, long id){
        List<Long> ids = questionDtoDao.getPaginationQuestionIdsWithoutAnswerWithIgnoredTags(page, size, id);
        Map<String, Object> parameters = setPaginationParameters(page, size, Optional.empty(), Optional.empty());
        parameters.put("page", page);
        parameters.put("size", size);
        parameters.put("ids", ids);
        parameters.put("id", id);
        return getPageDto(
                "paginationWithoutAnswersTrackedTag",
                parameters);
    }

}