/*
 * The MIT License (MIT)
 * Copyright © 2019-2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.meteor.example.consumer.web;

import com.sky.meteor.example.api.ExampleApi;
import com.sky.meteor.example.api.dto.UserDTO;
import com.sky.meteor.rpc.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * @author
 */
@Controller
@RequestMapping
@Slf4j
public class TestController {

    @Reference(group = "example")
    private ExampleApi exampleApi;

    @RequestMapping("test")
    @ResponseBody
    public UserDTO test() {
        UserDTO userDTO = UserDTO.builder().
                id(1L).
                name("sky")
                .build();
        UserDTO user = exampleApi.getUser(userDTO);
        log.info("user info :{}", user);
        return user;
    }

    @RequestMapping("testList")
    @ResponseBody
    public List<UserDTO> testList() {
        UserDTO userDTO = UserDTO.builder().
                id(1L).
                name("sky")
                .build();
        List<UserDTO> users = exampleApi.getUsers(userDTO);
        log.info("user list :{}", users);
        return users;
    }
}
