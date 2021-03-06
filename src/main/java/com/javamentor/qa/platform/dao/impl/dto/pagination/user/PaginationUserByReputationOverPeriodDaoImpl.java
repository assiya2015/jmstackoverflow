package com.javamentor.qa.platform.dao.impl.dto.pagination.user;

import com.javamentor.qa.platform.dao.abstracts.dto.pagination.PaginationDao;
import com.javamentor.qa.platform.dao.impl.dto.transformers.UserDtoListTranformer;
import com.javamentor.qa.platform.models.dto.UserDtoList;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

@Repository(value = "paginationUserByReputationOverPeriod")
@SuppressWarnings(value = "unchecked")
public class PaginationUserByReputationOverPeriodDaoImpl implements PaginationDao<UserDtoList> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<UserDtoList> getItems(Map<String, Object> parameters) {
        int page = (int) parameters.get("page");
        int size = (int) parameters.get("size");
        int days = (int) parameters.get("days");

        List<Long> usersIds = (List<Long>) em.unwrap(Session.class)
                .createQuery("select user.id " +
                        "from User user " +
                        "left outer join Reputation r on r.author.id=user.id " +
                        "where current_date - (:quantityOfDays)<date(r.persistDate)" +
                        "ORDER BY r.count DESC")
                .setParameter("quantityOfDays", days)
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .unwrap(org.hibernate.query.Query.class)
                .getResultList();

        return (List<UserDtoList>) em.unwrap(Session.class)
                .createQuery("select user.id as user_id, " +
                        "user.fullName as full_name, " +
                        "user.imageLink as link_image, " +
                        "(select coalesce(sum(ra.count), 0) from Reputation ra where ra.author.id = user.id) as reputation, " +
                        "tag.id as tag_id, " +
                        "tag.name as tag_name, " +
                        "tag.description as tag_description " +
                        "from User user " +
                        "left join Reputation r on user.id = r.author.id " +
                        "left join Question question on user.id=question.user.id " +
                        "left join question.tags tag " +
                        "where user.id in (:ids) " +
                        "group by user.id, user.fullName, user.imageLink, tag.id, tag.name " +
                        "order by reputation DESC"
                )
                .setParameter("ids", usersIds)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new UserDtoListTranformer())
                .getResultList();
    }

    @Override
    public int getCount(Map<String, Object> parameters) {
        return (int) (long) em.unwrap(Session.class).createQuery(
                "select count(user.id) " +
                        "from User user " +
                        "left outer join Reputation r on r.author.id=user.id " +
                        "where current_date - (:quantityOfDays)<date(r.persistDate)")
                .setParameter("quantityOfDays", (int) parameters.get("days"))
                .unwrap(org.hibernate.query.Query.class)
                .getSingleResult();
    }
}
