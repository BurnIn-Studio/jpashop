package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA",10000,  10);

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        //상품 주문시 상태는 Order
        assertEquals(OrderStatus.ORDER, getOrder.getStatus());

        //주문한 상품 종류 수가 정확해야 한다
        assertEquals(1, getOrder.getOrderItems().size());

        //주문 가격은 가격 * 수량이다.
        assertEquals(10000 * orderCount, getOrder.getTotalPrice());

        //주문 수량만큼 재고가 줄어야한다
        assertEquals(8, book.getStockQuantity());

    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA",10000,  10);

        int orderCount = 11;

        //when
        Assertions.assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), book.getId(), orderCount);
        });

        //then junit5여서 assertThrows로 대체한다
//        fail("재고 수량 예외가 터져야한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("시골 JPA", 10000, 10);

//        int orderCount = 2; 왜인지 안됨
        int orderCount = 5;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        //주문 취소시 상태는 CANCLE 이다
        assertEquals(OrderStatus.CANCLE, getOrder.getStatus());

        //주문이 취소된 상품은 그만큼 재고가 증가되야 한다.
        assertEquals(10, item.getStockQuantity());
    }

    @Test
    public void 재고수량초과() throws Exception {
        //given

        //when

        //then
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123")); em.persist(member);
        return member;
    }
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantity);
        book.setPrice(price);
        em.persist(book);
        return book;
    }

}