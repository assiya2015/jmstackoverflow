package com.javamentor.qa.platform.dao.impl.dto.pagination.user;

import com.javamentor.qa.platform.dao.abstracts.dto.pagination.PaginationDao;
import com.javamentor.qa.platform.dao.impl.dto.transformers.QuestionResultTransformer;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository(value = "userQuestionsOrderedByVotes")
public class UserQuestionsOrderedByVotesImpl implements PaginationDao<QuestionDto> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<QuestionDto> getItems(Map<String, Object> parameters) {
        int page = (int)parameters.get("page");
        int size = (int)parameters.get("size");
        long userId = (long)parameters.get("userId");
        List<Long> questionIds = (List<Long>) em.unwrap(Session.class)
                .createQuery("SELECT question.id " +
                        "FROM Question question " +
                        "WHERE question.user.id IN :userId ORDER BY question.voteQuestions.size DESC")
                .setParameter("userId", userId)
                .unwrap(org.hibernate.query.Query.class)
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();

        return (List<QuestionDto>) em.unwrap(Session.class)
                .createQuery("SELECT question.id AS question_id, " +
                        " question.title AS question_title," +
                        "u.fullName AS question_authorName," +
                        "u.id AS question_authorId, " +
                        "u.imageLink AS question_authorImage," +
                        "question.description AS question_description," +
                        " question.viewCount AS question_viewCount," +
                        "(SELECT COUNT (a.question.id) FROM Answer a WHERE a.question.id=question_id) AS question_countAnswer," +
                        "coalesce((select sum(v.vote) from VoteQuestion v where v.question.id = question.id), 0) AS question_countValuable," +
                        "question.persistDateTime AS question_persistDateTime, " +
                        "question.lastUpdateDateTime AS question_lastUpdateDateTime, " +
                        "tag.id AS tag_id,tag.name AS tag_name, tag.description as tag_description " +
                        "FROM Question question " +
                        "INNER JOIN  question.user u " +
                        "JOIN question.tags tag " +
                        "WHERE question.id IN :ids "+
                        "ORDER BY coalesce((select sum(v.vote) from VoteQuestion v where v.question.id = question.id), 0) DESC")
                .setParameter("ids", questionIds)
                .unwrap(Query.class)
                .setResultTransformer(new QuestionResultTransformer())
                .getResultList();
    }

    @Override
    public int getCount(Map<String, Object> parameters) {
        long userId = (long)parameters.get("userId");
        return (int) (long) em.createQuery("select count(q) from Question q where q.user.id in :userId").setParameter("userId", userId).getSingleResult();
    }

}