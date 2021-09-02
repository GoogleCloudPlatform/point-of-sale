// Copyright 2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.abmedge.apiserver.configuration;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class RedirectFilterConfiguration {

  private final Logger LOGGER = LoggerFactory.getLogger(RedirectFilterConfiguration.class);

  @Bean
  public FilterRegistrationBean<OncePerRequestFilter> spaRedirectFiler() {
    FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(createRedirectFilter());
    registration.addUrlPatterns("/*");
    registration.setName("apiServerRedirectFiler");
    registration.setOrder(1);
    return registration;
  }

  private OncePerRequestFilter createRedirectFilter() {
    //noinspection NullableProblems
    return new OncePerRequestFilter() {
      // Forwards all routes except '/index.html', '/200.html', '/favicon.ico', '/sw.js' '/api/', '/api/**'
      private final String REGEX = "(?!/actuator|/api|/_nuxt|/static|/index\\.html|/200\\.html|/favicon\\.ico|/sw\\.js).*$";
      private final Pattern pattern = Pattern.compile(REGEX);

      @Override
      protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
          FilterChain chain) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (pattern.matcher(uri).matches() && !uri.equals("/")) {
          // Delegate/Forward to `/` if `pattern` matches and it is not `/`
          // Read: https://github.com/jonashackt/spring-boot-vuejs#using-history-mode-for-nicer-urls
          if (!uri.endsWith("/ready") && !uri.endsWith("/healthy")) {
            // skip logging kube-proxy checks on /ready and /healthy
            LOGGER.info("URL {} entered directly into the Browser, redirecting...", uri);
          }
          RequestDispatcher rd = req.getRequestDispatcher("/");
          rd.forward(req, res);
        } else {
          chain.doFilter(req, res);
        }
      }
    };
  }
}
