package org.broadleafcommerce.order.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderImpl;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.broadleafcommerce.type.OrderType;
import org.springframework.stereotype.Repository;

@Repository("orderDao")
public class OrderDaoJpa implements OrderDao {

    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @PersistenceContext(name="blPU")
    private EntityManager em;

    @Resource
    private EntityConfiguration entityConfiguration;

    @Override
    @SuppressWarnings("unchecked")
    public Order readOrderById(Long orderId) {
        return (Order) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.order.domain.Order"), orderId);
    }

    @Override
    public Order maintianOrder(Order salesOrder) {
        if (salesOrder.getId() == null) {
            em.persist(salesOrder);
        } else {
            salesOrder = em.merge(salesOrder);
        }
        return salesOrder;
    }

    @Override
    public void deleteOrderForCustomer(Order salesOrder) {
        em.remove(salesOrder);
    }

    @Override
    public List<Order> readOrdersForCustomer(Customer customer) {
        return readOrdersForCustomer(customer.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order> readOrdersForCustomer(Long customerId) {
        Query query = em.createNamedQuery("BC_READ_ORDERS_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    @Override
    public Order readBasketOrdersForCustomer(Customer customer) {
        Order bo;
        Query query = em.createNamedQuery("BC_READ_ORDERS_BY_CUSTOMER_ID_AND_TYPE");
        query.setParameter("customerId", customer.getId());
        query.setParameter("orderType", OrderType.BASKET);
        try {
            bo = (OrderImpl) query.getSingleResult();
            return (OrderImpl) query.getSingleResult();
        } catch (NoResultException nre) {
            bo = (Order) entityConfiguration.createEntityInstance("org.broadleafcommerce.order.domain.Order");
            bo.setCustomer(customer);
            bo.setType(OrderType.BASKET);
            em.persist(bo);
            return bo;
        }
    }

    @Override
    public Order submitOrder(Order basketOrder) {
        OrderImpl so = new OrderImpl();
        so.setId(basketOrder.getId());
        Query query = em.createNamedQuery("BC_UPDATE_BASKET_ORDER_TO_SUBMITTED");
        query.setParameter("id", basketOrder.getId());
        query.executeUpdate();
        return em.find(OrderImpl.class, so.getId());
    }

    public Order create() {
        return ((Order) entityConfiguration.createEntityInstance("org.broadleafcommerce.order.domain.OrderImpl"));
    }
}
