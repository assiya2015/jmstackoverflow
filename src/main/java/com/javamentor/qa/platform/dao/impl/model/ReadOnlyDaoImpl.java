package com.javamentor.qa.platform.dao.impl.model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class ReadOnlyDaoImpl<E, K> {

    @PersistenceContext
    private EntityManager entityManager;

    protected Class<E> genericClass;

    public ReadOnlyDaoImpl() {
        this.genericClass = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }


    public List<E> getAll() {
        return null;
    }

    public boolean existsById(K id) {
        int count = (int) entityManager.createQuery("SELECT COUNT(e) FROM " + genericClass.getName() + " e WHERE e.id =: id").setParameter("id", id).getSingleResult();
        return count == 1;
    }

    public Optional<E> getById(K id) {
        return Optional.empty();
    }

    public List<E> getAllByIds(Iterable<K> ids) {
        return null;
    }

    public boolean existsByAllIds(Collection<K> ids) {
        return false;
    }
}
