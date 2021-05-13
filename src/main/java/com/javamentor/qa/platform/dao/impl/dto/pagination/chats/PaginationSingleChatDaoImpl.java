package com.javamentor.qa.platform.dao.impl.dto.pagination.chats;

import com.javamentor.qa.platform.dao.abstracts.dto.pagination.PaginationDao;
import com.javamentor.qa.platform.dao.impl.dto.transformers.SingleChatResultTransformer;
import com.javamentor.qa.platform.models.dto.SingleChatDto;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Repository(value = "paginationSingleChat")

public class PaginationSingleChatDaoImpl implements PaginationDao<SingleChatDto> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SingleChatDto> getItems(Map<String, Object> parameters) {
        int page = (int)parameters.get("page");
        int size = (int)parameters.get("size");
        long userId = (long)parameters.get("userId");

         return entityManager.unwrap(Session.class)
                .createNativeQuery("SELECT distinct  ch.id, " +
                        "ch.title, " +
                        "sc.user_one_id, " +
                        "sc.use_two_id, " +
                        "u.full_name, " +
                        "u.image_link, " +
                        "(select m.message from Message m where ch.id = m.chat_id order by m.last_redaction_date desc fetch first row only ), " +
                        "(select m.user_sender_id from Message m where ch.id = m.chat_id order by m.last_redaction_date desc fetch first row only ), " +
                        "(select m.last_redaction_date from Message m where ch.id = m.chat_id order by m.last_redaction_date desc fetch first row only ) as lastredactiondate " +
                        "FROM Chat ch " +
                        "join singel_chat sc on sc.chat_id = ch.id " +
                        "join user_entity u on u.id = (select m.user_sender_id from Message m where ch.id = m.chat_id order by m.last_redaction_date desc fetch first row only ) " +
                        "where  sc.chat_id = ch.id and (sc.user_one_id = :userId or sc.use_two_id = :userId )" +
                        "order by lastredactiondate desc ")
                 .unwrap(Query.class)
                 .setParameter("userId", userId)
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .setResultTransformer(new SingleChatResultTransformer())
                .getResultList();

    }

    @Override
    public int getCount(Map<String, Object> parameters) {
        long userId = (long)parameters.get("userId");
        return (int)(long) entityManager.createQuery("select count(sc) from SingleChat sc " +
                "where sc.userOne.id = :userId or sc.useTwo.id = :userId ")
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
