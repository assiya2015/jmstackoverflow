package com.javamentor.qa.platform.webapp.controllers;

import com.javamentor.qa.platform.models.dto.*;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.CommentAnswer;
import com.javamentor.qa.platform.models.entity.question.answer.VoteAnswer;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.security.util.SecurityHelper;
import com.javamentor.qa.platform.service.abstracts.dto.AnswerDtoService;
import com.javamentor.qa.platform.service.abstracts.dto.VoteAnswerDtoService;
import com.javamentor.qa.platform.service.abstracts.model.*;
import com.javamentor.qa.platform.webapp.converters.AnswerConverter;
import com.javamentor.qa.platform.webapp.converters.CommentConverter;
import com.javamentor.qa.platform.models.dto.CommentDto;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.service.abstracts.dto.CommentDtoService;
import com.javamentor.qa.platform.service.abstracts.model.AnswerService;
import com.javamentor.qa.platform.webapp.converters.VoteAnswerConverter;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/question")
@Api(value = "AnswerApi")
public class AnswerController {
    private final AnswerService answerService;
    private final CommentAnswerService commentAnswerService;
    private final CommentConverter commentConverter;
    private final SecurityHelper securityHelper;
    private final CommentDtoService commentDtoService;
    private final QuestionService questionService;
    private final VoteAnswerDtoService voteAnswerDtoService;

    private final AnswerConverter answerConverter;
    private final VoteAnswerService voteAnswerService;
    private final VoteAnswerConverter voteAnswerConverter;
    private final AnswerDtoService answerDtoService;

    @Autowired
    public AnswerController(AnswerService answerService,
                            CommentAnswerService commentAnswerService,
                            CommentConverter commentConverter,
                            SecurityHelper securityHelper,
                            CommentDtoService commentDtoService,
                            QuestionService questionService,
                            VoteAnswerDtoService voteAnswerDtoService,
                            AnswerConverter answerConverter,
                            VoteAnswerService voteAnswerService,
                            VoteAnswerConverter voteAnswerConverter,
                            AnswerDtoService answerDtoService) {
        this.answerService = answerService;
        this.commentAnswerService = commentAnswerService;
        this.commentConverter = commentConverter;
        this.securityHelper = securityHelper;
        this.commentDtoService = commentDtoService;
        this.questionService = questionService;
        this.voteAnswerDtoService = voteAnswerDtoService;
        this.answerConverter = answerConverter;
        this.voteAnswerService = voteAnswerService;
        this.voteAnswerConverter = voteAnswerConverter;
        this.answerDtoService = answerDtoService;
    }


    @PostMapping("/{questionId}/answer/{answerId}/comment")
    @ApiOperation(value = "Add comment", notes = "This method Add comment to answer and return CommentDto")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Comment was added", response = CommentDto.class),
            @ApiResponse(code = 400, message = "Answer or question not found", response = String.class)
    })
    public ResponseEntity<?> addCommentToAnswer(
            @ApiParam(name = "AnswerId", value = "AnswerId. Type long", required = true, example = "1")
            @PathVariable Long answerId,
            @ApiParam(name = "QuestionId", value = "QuestionId. Type long", required = true, example = "1")
            @PathVariable Long questionId,
            @ApiParam(name = "text", value = "Text of comment. Type string", required = true, example = "Some comment")
            @RequestBody String commentText) {

        User user = securityHelper.getPrincipal();

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer not found");
        }

        CommentAnswer commentAnswer = commentAnswerService.addCommentToAnswer(commentText, answer.get(), user);

        return ResponseEntity.ok(commentConverter.commentToCommentDTO(commentAnswer));
    }

    @GetMapping("/{questionId}/answer/{answerId}/comments")
    @ApiOperation(value = "Return all Comments by answerID", notes = "Return all Comments by answerID")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Return all Comments by answerID", response = CommentDto.class,  responseContainer = "List"),
            @ApiResponse(code = 400, message = "Answer not found", response = String.class)
    })

    public ResponseEntity<?> getCommentListByAnswerId(
            @ApiParam(name = "questionId", value = "questionId. Type Long", required = true, example = "0")
            @PathVariable Long questionId,
            @ApiParam(name = "answerId", value = "answerId. Type long", required = true, example = "1")
            @PathVariable Long answerId) {

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question was not found");
        }

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer not found");
        }

        List<CommentAnswerDto> commentAnswerDtoList = commentDtoService.getAllCommentsByAnswerId(answerId);

        return ResponseEntity.ok(commentAnswerDtoList);
    }

    @GetMapping("/{questionId}/answer")
    @ApiOperation(value = "Return List<AnswerDto> with answers for question", notes = "This method return List<AnswerDto> with answers with has presented questionId")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Return answers for question", response = AnswerDto.class,  responseContainer = "List"),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })

    public ResponseEntity<?> getAnswerListByQuestionId(@ApiParam(name = "questionId", value = "questionId. Type long", required = true, example = "1")
                                                       @PathVariable Long questionId) {

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        List<AnswerDto> answerDtoList = answerDtoService.getAllAnswersByQuestionId(questionId);

        return ResponseEntity.ok(answerDtoList);
    }

    @PostMapping("/{questionId}/answer")
    @ApiOperation(value = "Add answer", notes = "This method Add answer to question and return AnswerDto")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Answer was added", response = AnswerDto.class),
            @ApiResponse(code = 400, message = "Question or user not found", response = String.class)
    })

    public ResponseEntity<?> addAnswerToQuestion(@Valid @RequestBody CreateAnswerDto createAnswerDto,
                                                 @ApiParam(name = "questionId", value = "questionId. Type long", required = true, example = "1")
                                                 @PathVariable Long questionId) {


        User user = securityHelper.getPrincipal();

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        Answer answer = new Answer(question.get(), user, createAnswerDto.getHtmlBody(), false, false);
        answer.setQuestion(question.get());

        answerService.persist(answer);

        return ResponseEntity.ok(answerConverter.answerToAnswerDTO(answer));
    }

    @PatchMapping("/{questionId}/answer/{answerId}/upVote")
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 200, message = "Answer was up voted", response = VoteAnswerDto.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })
    public ResponseEntity<?> answerUpVote(
            @ApiParam(name = "questionId", value = "type Long", required = true, example = "0")
            @PathVariable Long questionId,
            @ApiParam(name = "answerId", value = "type Long", required = true, example = "0")
            @PathVariable Long answerId) {


        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question was not found");
        }

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer was not found");
        }

        VoteAnswer voteAnswer = new VoteAnswer(securityHelper.getPrincipal(), answer.get(), 1);
        voteAnswerService.persist(voteAnswer);

        return ResponseEntity.ok(voteAnswerConverter.voteAnswerToVoteAnswerDto(voteAnswer));
    }

    @PatchMapping("/{questionId}/answer/{answerId}/downVote")
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 200, message = "Answer was up voted", response = VoteAnswerDto.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class)
    })
    public ResponseEntity<?> answerDownVote(
            @ApiParam(name = "questionId", value = "type Long", required = true, example = "0")
            @PathVariable Long questionId,
            @ApiParam(name = "answerId", value = "type Long", required = true, example = "0")
            @PathVariable Long answerId) {


        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question was not found");
        }

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer was not found");
        }

        VoteAnswer voteAnswer = new VoteAnswer(securityHelper.getPrincipal(), answer.get(), -1);
        voteAnswerService.persist(voteAnswer);

        return ResponseEntity.ok(voteAnswerConverter.voteAnswerToVoteAnswerDto(voteAnswer));
    }

    @GetMapping("/{questionId}/isAnswerVoted")
    @ApiOperation(value = "Checks if user vote up answer to the question",
            notes = "Provide an question ID, to check, if current user have already voted up ANY answer to this question",
            response = Boolean.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "True, if user voted; False, if not", response = Boolean.class),
            @ApiResponse(code = 400, message = "Question not found", response = String.class),
    })
    public ResponseEntity<?> isAnyAnswerVotedByCurrentUser(@ApiParam(name = "questionId", value = "ID value, for the question, the answer to which needs to be check", required = true, example = "1")
                                                     @PathVariable Long questionId) {

        Optional<Question> question = questionService.getById(questionId);

        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        User user = securityHelper.getPrincipal();

        Optional<VoteAnswerDto> vote = voteAnswerDtoService.getVoteByQuestionIdAndUserId(questionId, user.getId());

        if (vote.isPresent()) { return ResponseEntity.ok(true); }

        return ResponseEntity.ok(false);
    }

}
