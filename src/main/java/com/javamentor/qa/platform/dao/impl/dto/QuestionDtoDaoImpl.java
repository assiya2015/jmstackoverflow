package com.javamentor.qa.platform.dao.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.QuestionDtoDao;
import com.javamentor.qa.platform.dao.impl.dto.transformers.QuestionResultTransformer;
import com.javamentor.qa.platform.dao.impl.dto.transformers.QuestionResultTransformerTagOnly;
import com.javamentor.qa.platform.dao.impl.dto.transformers.QuestionResultTransformerWithoutTag;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import com.javamentor.qa.platform.models.entity.question.Question;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class QuestionDtoDaoImpl implements QuestionDtoDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<QuestionDto> getQuestionDtoById(Long id) {

        return (Optional<QuestionDto>) entityManager.unwrap(Session.class)
                .createQuery("select question.id as question_id, " +
                        " question.title as question_title," +
                        "u.fullName as question_authorName," +
                        " u.id as question_authorId, " +
                        "u.imageLink as question_authorImage," +
                        "question.description as question_description," +
                        " question.viewCount as question_viewCount," +
                        "(select count(a.question.id) from Answer a where a.question.id=:id) as question_countAnswer," +
                        "(select count(v.question.id) from VoteQuestion v where v.question.id=:id) as question_countValuable," +
                        "question.persistDateTime as question_persistDateTime," +
                        "question.lastUpdateDateTime as question_lastUpdateDateTime, " +
                        " tag.id as tag_id,tag.name as tag_name " +
                        "from Question question  " +
                        "INNER JOIN  question.user u" +
                        "  join question.tags tag"
                        + " where question.id=:id")
                .setParameter("id", id)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new QuestionResultTransformer())
                .uniqueResultOptional();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<Question> getPagination(int page, int size) {

        return entityManager.createQuery("from Question ")
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<Question> getPaginationPopular(int page, int size) {

        return entityManager.createQuery("select q from Question q order by q.viewCount desc")
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<QuestionDto> getQuestionDtoByTagIds(List<Long> ids) {

        return (List<QuestionDto>) entityManager.unwrap(Session.class)
                .createQuery("select question.id as question_id, " +
                        " question.title as question_title," +
                        "u.fullName as question_authorName," +
                        " u.id as question_authorId, " +
                        "u.imageLink as question_authorImage," +
                        "question.description as question_description," +
                        " question.viewCount as question_viewCount," +
                        "(select count(a.question.id) from Answer a where a.question.id=question_id) as question_countAnswer," +
                        "(select count(v.question.id) from VoteQuestion v where v.question.id=question_id) as question_countValuable," +
                        "question.persistDateTime as question_persistDateTime," +
                        "question.lastUpdateDateTime as question_lastUpdateDateTime, " +
                        " tag.id as tag_id,tag.name as tag_name " +
                        "from Question question  " +
                        "INNER JOIN  question.user u" +
                        "  join question.tags tag" +
                        " where question_id IN :ids order by question.viewCount desc")
                .setParameter("ids", ids)
                .unwrap(Query.class)
                .setResultTransformer(new QuestionResultTransformer())
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getNoAnsweredQuestionsIDs(int page, int size) {
        List<Long> listAnswerIDs = (List<Long>) entityManager.createQuery("select a.question.id from Answer a")
                .getResultList();
        List<Long> listAllIDs = (List<Long>) entityManager.createQuery("select q.id from Question q")
                .getResultList();

        return listAllIDs.stream().filter(e -> !listAnswerIDs.contains(e)).skip(page * size - size).limit(size).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public long getAllNoAnswerQuestionCount() {
        List<Long> listAnswerIDs = (List<Long>) entityManager.createQuery("select a.question.id from Answer a")
                .getResultList();
        List<Long> listAllIDs = (List<Long>) entityManager.createQuery("select q.id from Question q")
                .getResultList();
        return listAllIDs.stream().filter(e -> !listAnswerIDs.contains(e)).count();
    }

    @Override
    public int getTotalResultCountQuestionDto() {
        long totalResultCount = (long) entityManager.createQuery("select count(*) from Question").getSingleResult();
        return (int) totalResultCount;
    }

    @Override
    public List<QuestionDto> getPaginationOrderedNew(int page, int size) {
        List<QuestionDto> questionDtoList = entityManager.unwrap(Session.class)
                .createQuery(
                        "select question.id as question_id, " +
                                "question.title as question_title, " +
                                "u.fullName as question_authorName, " +
                                "u.id as question_authorId, " +
                                "u.imageLink as question_authorImage, " +
                                "question.description as question_description, " +
                                "question.viewCount as question_viewCount, " +
                                "(select count(a.question.id) from Answer a where question_id=a.question.id) as question_countAnswer, " +
                                "(select count(v.question.id) from VoteQuestion v where question_id=v.question.id) as question_countValuable, " +
                                "question.persistDateTime as question_persistDateTime, " +
                                "question.lastUpdateDateTime as question_lastUpdateDateTime " +
                                "from Question question " +
                                "INNER JOIN question.user u " +
                                "order by question_lastUpdateDateTime desc "
                )
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new QuestionResultTransformerWithoutTag())
                .getResultList();

        return questionDtoList;
    }

    @Override
    public List<QuestionDto> getQuestionTagsByQuestionIds(List<Long> ids) {
        List<QuestionDto> tagsByIds = entityManager.unwrap(Session.class)
                .createQuery(
                        "select question.id as question_id," +
                                "tag.id as tag_id," +
                                "tag.name as tag_name " +
                                "from Question question " +
                                "inner join question.user u " +
                                "join question.tags tag " +
                                "where question_id IN :ids"
                )
                .setParameter("ids", ids)
                .setResultTransformer(new QuestionResultTransformerTagOnly())
                .getResultList();
        return tagsByIds;
    }

}