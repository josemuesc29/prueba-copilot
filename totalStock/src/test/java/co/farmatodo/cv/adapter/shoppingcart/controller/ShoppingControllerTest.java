package co.farmatodo.cv.adapter.shoppingcart.controller;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
public class ShoppingControllerTest extends TestCase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoot() throws Exception {
        mockMvc.perform(get("/shopping-cart/v1/")).andDo(print()).andExpect(status().isOk()).andExpect(content().string(notNullValue()));
    }

    @Test
    public void testPing() throws Exception {
        mockMvc.perform(get("/shopping-cart/v1/ping")).andDo(print()).andExpect(status().isOk()).andExpect(content().string(notNullValue()));
    }
}
