package com.tmforum.openapi.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {
    
    @Test
    void testOf_FirstPage() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageResponse<String> response = PageResponse.of(content, 0, 10, 25);
        
        assertNotNull(response);
        assertEquals(3, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }
    
    @Test
    void testOf_LastPage() {
        List<String> content = Arrays.asList("item21", "item22");
        PageResponse<String> response = PageResponse.of(content, 2, 10, 22);
        
        assertNotNull(response);
        assertEquals(2, response.getPage());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }
    
    @Test
    void testOf_MiddlePage() {
        List<String> content = Arrays.asList("item11", "item12");
        PageResponse<String> response = PageResponse.of(content, 1, 10, 22);
        
        assertNotNull(response);
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }
    
    @Test
    void testOf_EmptyContent() {
        List<String> content = Collections.emptyList();
        PageResponse<String> response = PageResponse.of(content, 0, 10, 0);
        
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }
    
    @Test
    void testOf_SinglePage() {
        List<String> content = Arrays.asList("item1", "item2");
        PageResponse<String> response = PageResponse.of(content, 0, 10, 2);
        
        assertNotNull(response);
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }
    
    @Test
    void testOf_ExactPageSize() {
        List<String> content = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        PageResponse<String> response = PageResponse.of(content, 0, 5, 5);
        
        assertEquals(5, response.getContent().size());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
    
    @Test
    void testOf_LargeTotalElements() {
        List<String> content = Arrays.asList("item1");
        PageResponse<String> response = PageResponse.of(content, 0, 10, 100);
        
        assertEquals(10, response.getTotalPages());
        assertTrue(response.isHasNext());
    }
    
    @Test
    void testOf_SecondPageOfThree() {
        List<String> content = Arrays.asList("item11", "item12");
        PageResponse<String> response = PageResponse.of(content, 1, 10, 25);
        
        assertEquals(1, response.getPage());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }
    
    @Test
    void testOf_PageWithRemainder() {
        List<String> content = Arrays.asList("item21", "item22", "item23");
        PageResponse<String> response = PageResponse.of(content, 2, 10, 23);
        
        assertEquals(2, response.getPage());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isLast());
    }
    
    @Test
    void testNoArgsConstructor() {
        PageResponse<String> response = new PageResponse<>();
        assertNotNull(response);
        assertNull(response.getContent());
        assertEquals(0, response.getPage());
        assertEquals(0, response.getSize());
    }
    
    @Test
    void testAllArgsConstructor() {
        List<String> content = Arrays.asList("item1", "item2");
        PageResponse<String> response = new PageResponse<>(
            content, 0, 10, 20, 2, true, false, true, false
        );
        
        assertEquals(content, response.getContent());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(20, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }
    
    @Test
    void testSettersAndGetters() {
        PageResponse<String> response = new PageResponse<>();
        List<String> content = Arrays.asList("item1");
        
        response.setContent(content);
        response.setPage(1);
        response.setSize(5);
        response.setTotalElements(10);
        response.setTotalPages(2);
        response.setFirst(false);
        response.setLast(false);
        response.setHasNext(true);
        response.setHasPrevious(true);
        
        assertEquals(content, response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(10, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }
    
    @Test
    void testEquals() {
        List<String> content1 = Arrays.asList("item1");
        List<String> content2 = Arrays.asList("item1");
        List<String> content3 = Arrays.asList("item2");
        
        PageResponse<String> response1 = new PageResponse<>(
            content1, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response2 = new PageResponse<>(
            content2, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response3 = new PageResponse<>(
            content3, 1, 10, 10, 1, false, true, false, true
        );
        PageResponse<String> response4 = new PageResponse<>(
            null, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response5 = new PageResponse<>(
            null, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response6 = new PageResponse<>(
            content1, 0, 10, 10, 1, true, true, false, false
        );
        
        // Equals
        assertEquals(response1, response2);
        assertEquals(response1, response1); // Reflexive
        assertEquals(response4, response5);
        
        // Different
        assertNotEquals(response1, response3);
        assertNotEquals(response1, new PageResponse<>(content1, 1, 10, 10, 1, false, true, false, true));
        assertNotEquals(response1, new PageResponse<>(content1, 0, 20, 10, 1, true, true, false, false));
        assertNotEquals(response1, new PageResponse<>(content1, 0, 10, 20, 2, true, false, true, false));
        assertNotEquals(response1, new PageResponse<>(content1, 0, 10, 10, 1, false, true, false, false));
        assertNotEquals(response1, new PageResponse<>(content1, 0, 10, 10, 1, true, false, true, false));
        assertNotEquals(response1, new PageResponse<>(content1, 0, 10, 10, 1, true, true, true, false));
        assertNotEquals(response1, new PageResponse<>(content1, 0, 10, 10, 1, true, true, false, true));
        assertNotEquals(response4, response6);
        
        // Null and different types.
        assertNotEquals(response1, null);
        assertNotEquals(response1, "not a PageResponse");
    }
    
    @Test
    void testHashCode() {
        List<String> content1 = Arrays.asList("item1");
        List<String> content2 = Arrays.asList("item1");
        
        PageResponse<String> response1 = new PageResponse<>(
            content1, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response2 = new PageResponse<>(
            content2, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response3 = new PageResponse<>(
            null, 0, 10, 10, 1, true, true, false, false
        );
        PageResponse<String> response4 = new PageResponse<>(
            null, 0, 10, 10, 1, true, true, false, false
        );
        
        // Equal objects must have the same hashCode
        assertEquals(response1.hashCode(), response2.hashCode());
        assertEquals(response3.hashCode(), response4.hashCode());
        
        // Consistency
        assertEquals(response1.hashCode(), response1.hashCode());
        
        // equals/hashCode contract
        assertTrue(response1.equals(response2));
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testToString() {
        List<String> content = Arrays.asList("item1", "item2");
        PageResponse<String> response = PageResponse.of(content, 0, 10, 2);
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PageResponse"));
    }
}

