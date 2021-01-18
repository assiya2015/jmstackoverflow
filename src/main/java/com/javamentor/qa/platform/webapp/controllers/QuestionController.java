package com.javamentor.qa.platform.webapp.controllers;

import com.javamentor.qa.platform.models.dto.*;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.models.entity.question.Tag;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.AnswerVote;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.models.util.OnCreate;
import com.javamentor.qa.platform.security.util.SecurityHelper;
import com.javamentor.qa.platform.service.abstracts.dto.QuestionDtoService;
import com.javamentor.qa.platform.service.abstracts.dto.UserDtoService;
import com.javamentor.qa.platform.service.abstracts.model.AnswerService;
import com.javamentor.qa.platform.service.abstracts.model.QuestionService;
import com.javamentor.qa.platform.service.abstracts.model.*;

import com.javamentor.qa.platform.service.abstracts.model.TagService;
import com.javamentor.qa.platform.service.abstracts.model.UserService;
import com.javamentor.qa.platform.webapp.converters.AnswerConverter;
import com.javamentor.qa.platform.webapp.converters.AnswerVoteConverter;
import com.javamentor.qa.platform.webapp.converters.QuestionConverter;
import com.javamentor.qa.platform.webapp.converters.TagMapper;
import com.javamentor.qa.platform.webapp.converters.UserConverter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/question")
@Api(value = "QuestionApi")
public class QuestionController {

    private final QuestionService questionService;
    private final TagMapper tagMapper;
    private final TagService tagService;
    private final UserDtoService userDtoService;
    private final AnswerService answerService;
    private final AnswerConverter answerConverter;
    private final SecurityHelper securityHelper;
    private final AnswerVoteService answerVoteService;
    private final AnswerVoteConverter answerVoteConverter;


    private final QuestionDtoService questionDtoService;

    private static final int MAX_ITEMS_ON_PAGE = 100;

    @Autowired
    public QuestionController(QuestionService questionService,
                              TagMapper tagMapper,
                              TagService tagService,
                              QuestionDtoService questionDtoService,
                              UserDtoService userDtoService,
                              AnswerConverter answerConverter,
                              SecurityHelper securityHelper,
                              AnswerVoteService answerVoteService,
                              AnswerService answerService,
                              AnswerVoteConverter answerVoteConverter) {
        this.questionService = questionService;
        this.tagMapper = tagMapper;
        this.tagService = tagService;
        this.questionDtoService = questionDtoService;
        this.userDtoService = userDtoService;
        this.answerConverter = answerConverter;
        this.securityHelper = securityHelper;
        this.answerVoteService = answerVoteService;
        this.answerService = answerService;
        this.answerVoteConverter = answerVoteConverter;
    }

    @Autowired
    public QuestionConverter questionConverter;

    @Autowired
    public UserConverter userConverter;

    @Autowired
    public UserService userService;


    @DeleteMapping("/{id}/delete")
    @ApiOperation(value = "Delete question", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Deletes the question.", response = String.class),
            @ApiResponse(code = 400, message = "Wrong ID", response = String.class)
    })
    public ResponseEntity<String> deleteQuestionById(@ApiParam(name = "id") @PathVariable Long id) {

        Optional<Question> question = questionService.getById(id);
        if (question.isPresent()) {
            questionService.delete(question.get());
            return ResponseEntity.ok("Question was deleted");
        } else {
            return ResponseEntity.badRequest().body("Wrong ID");
        }
    }

    @SneakyThrows
    @PatchMapping("/{questionId}/tag/add")
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 200, message = "Tags were added", response = String.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })
    public ResponseEntity<?> setTagForQuestion(
            @ApiParam(name = "questionId", value = "type Long", required = true, example = "0")
            @PathVariable Long questionId,
            @ApiParam(name = "tagId", value = "type List<Long>", required = true)
            @RequestBody List<Long> tagId) {

        if (questionId == null) {
            return ResponseEntity.badRequest().body("Question id is null");
        }

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        if (tagService.existsByAllIds(tagId)) {
            tagService.addTagToQuestion(tagId, question.get());
            return ResponseEntity.ok().body("Tags were added");
        }

        return ResponseEntity.badRequest().body("Tag not found");
    }


    @GetMapping("/{id}")
    @ApiOperation(value = "get QuestionDto", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the QuestionDto", response = QuestionDto.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })

    public ResponseEntity<?> getQuestionById(
            @ApiParam(name = "id", value = "type Long", required = true, example = "0")
            @PathVariable Long id) {

        Optional<QuestionDto> questionDto = questionDtoService.getQuestionDtoById(id);

        return questionDto.isPresent() ? ResponseEntity.ok(questionDto.get()) :
                ResponseEntity.badRequest().body("Question not found");
    }

    @GetMapping(path = "/",
            params = {"page", "size"}
    )
    @ApiOperation(value = "Return object(PageDto<QuestionDto, Object>)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination List<QuestionDto>"),
    })
    public ResponseEntity<?> findPagination(

            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }
        PageDto<QuestionDto, Object> resultPage = questionDtoService.getPagination(page, size);

        return ResponseEntity.ok(resultPage);
    }

    @GetMapping(value = "/popular", params = {"page", "size"})
    @ApiOperation(value = "Return object(PageDto<QuestionDto, Object>)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination popular List<QuestionDto>"),
    })
    public ResponseEntity<?> findPaginationPopular(

            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }
        PageDto<QuestionDto, Object> resultPage = questionDtoService.getPaginationPopular(page, size);

        return ResponseEntity.ok(resultPage);
    }


    @PostMapping("/add")
    @Validated(OnCreate.class)
    @ResponseBody
    @ApiOperation(value = "add Question", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Add Question", response = Question.class),
            @ApiResponse(code = 400, message = "Question not add", response = String.class)
    })
    public ResponseEntity<?> addQuestion(@Valid @RequestBody QuestionCreateDto questionCreateDto) {

        if (!userService.existsById(questionCreateDto.getUserId())) {
            return ResponseEntity.badRequest().body("questionCreateDto.userId dont`t exist");
        }

        Question question = questionConverter.questionCreateDtoToQuestion(questionCreateDto);
        questionService.persist(question);

        QuestionDto questionDtoNew = questionConverter.questionToQuestionDto(question);

        return ResponseEntity.ok(questionDtoNew);
    }


    @GetMapping(value = "order/new", params = {"page", "size"})
    @ApiOperation(value = "Return object(PageDto<QuestionDto, Object>)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination popular List<QuestionDto>"),
    })
    public ResponseEntity<?> findPaginationOrderedNew(

            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<QuestionDto, Object> resultPage = questionDtoService.getPaginationOrderedNew(page, size);


        return ResponseEntity.ok(resultPage);
    }

    @GetMapping(value = "/withoutAnswer", params = {"page", "size"})
    @ApiOperation(value = "Return Questions without answers")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination List<QuestionDto>"),
    })
    public ResponseEntity<?> getQuestionsWithoutAnswer(
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    required = true,
                    example = "10")
            @RequestParam("size") int size) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }
        PageDto<QuestionDto, Object> resultPage = questionDtoService.getPaginationWithoutAnswers(page, size);
        return ResponseEntity.ok(resultPage);
    }

    @GetMapping(value = "/withTags", params = {"page", "size"})
    @ApiOperation(value = "Return questions that include all given tags")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Return the pagination PageDto", response = PageDto.class),
            @ApiResponse(code = 400, message = "QuestionList with given TagIds not found.")
    })
    public ResponseEntity<?> getQuestionsWithGivenTags(
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    required = true,
                    example = "10")
            @RequestParam("size") int size,
            @ApiParam(name = "tagIds", required = true, type = "List<Long>")
            @Valid
            @RequestBody List<Long> tagIds
    ) {
        if (size <= 0 || page <= 0 || size > MAX_ITEMS_ON_PAGE) {
            ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<QuestionDto, Object> resultPage = questionDtoService.getPAginationWithGivenTags(page, size, tagIds);

        if (resultPage.getItems().isEmpty()) {
            ResponseEntity.notFound();
        }
        return ResponseEntity.ok(resultPage);
    }

    @PostMapping(value = "/withoutTags", params = {"page", "size"})
    @ApiOperation(value = "Return object(PageDto<QuestionDto, Object>)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination without tags List<QuestionDto>"),
            @ApiResponse(code = 400, message = "QuestionList with given TagIds not found.")
    })
    public ResponseEntity<?> findPaginationWithoutTags(
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size,
            @ApiParam(name = "tagIds", required = true, type = "List<Long>", value = "List of id tags to be deleted")
            @RequestBody List<Long> tagIds) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<QuestionDto, Object> resultPage = questionDtoService.getPaginationWithoutTags(page, size, tagIds);

        return ResponseEntity.ok(resultPage);
    }

    @GetMapping(value = "/search", params = {"page", "size"})
    @ApiOperation(value = "Return Questions by search value")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination List<QuestionDto>"),
            @ApiResponse(code = 400, message = "Bad Request", response = String.class)
    })
    public ResponseEntity<?> qetQuestionBySearch(
            @Valid @RequestBody QuestionSearchDto questionSearchDto,
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    required = true, example = "10")
            @RequestParam("size") int size) {

        if (size <= 0 || page <= 0 || size > MAX_ITEMS_ON_PAGE) {
            ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<QuestionDto, Object> resultPage =
                questionDtoService.getQuestionBySearchValue(questionSearchDto.getMessage(), page, size);
        return ResponseEntity.ok(resultPage);
    }


    @PostMapping("{questionId}/answer")
    @ApiOperation(value = "Add answer", notes = "This method Add answer to question and return AnswerDto")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Answer was added", response = AnswerDto.class),
            @ApiResponse(code = 400, message = "Question or user not found", response = String.class)
    })

    public ResponseEntity<?> addAnswerToQuestion(@Valid @RequestBody CreateAnswerDto createAnswerDto,
                                                 @ApiParam(name = "QuestionId", value = "QuestionId. Type long", required = true, example = "1")
                                                 @PathVariable Long questionId) {


        Optional<User> user = userService.getById(securityHelper.getPrincipal().getId());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        Answer answer = new Answer(question.get(), user.get(), createAnswerDto.getHtmlBody(), false, false);
        answer.setQuestion(question.get());

        answerService.persist(answer);

        return ResponseEntity.ok(answerConverter.answerToAnswerDTO(answer));
    }

    @PatchMapping("/{questionId}/answer/{answerId}/upVote")
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 200, message = "Answer was up voted", response = AnswerVoteDto.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })
    public ResponseEntity<?> answerUpVote(
            @ApiParam(name = "questionId", value = "type Long", required = true, example = "0")
            @PathVariable Long questionId,
            @ApiParam(name = "answerId", value = "type Long", required = true, example = "0")
            @PathVariable Long answerId) {

        if (questionId == null) {
            return ResponseEntity.badRequest().body("Question id is null");
        }


        Optional<User> user = userService.getById(securityHelper.getPrincipal().getId());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer not found");
        }

        AnswerVote answerVote = new AnswerVote(user.get(), answer.get(), 1);
        answerVoteService.persist(answerVote);

        return ResponseEntity.ok(answerVoteConverter.answerVoteToAnswerVoteDto(answerVote));
    }

    @PatchMapping("/{questionId}/answer/{answerId}/downVote")
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 200, message = "Answer was up voted", response = AnswerVoteDto.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })
    public ResponseEntity<?> answerDownVote(
            @ApiParam(name = "questionId", value = "type Long", required = true, example = "0")
            @PathVariable Long questionId,
            @ApiParam(name = "answerId", value = "type Long", required = true, example = "0")
            @PathVariable Long answerId) {

        if (questionId == null) {
            return ResponseEntity.badRequest().body("Question id is null");
        }


        Optional<User> user = userService.getById(securityHelper.getPrincipal().getId());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer not found");
        }

        AnswerVote answerVote = new AnswerVote(user.get(), answer.get(), -1);
        answerVoteService.persist(answerVote);

        return ResponseEntity.ok(answerVoteConverter.answerVoteToAnswerVoteDto(answerVote));
    }
}