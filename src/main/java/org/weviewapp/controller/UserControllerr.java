package org.weviewapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/principal")
public class UserControllerr {
    @PostMapping("/call-python-api")
    public String callPythonAPI(@RequestBody String requestBody) {
        try {
            String pythonApiUrl = "http://localhost:5000/api/example"; // Replace with your Python API URL

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(pythonApiUrl);
            StringEntity params = new StringEntity(requestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
//            ResponseModel responseModel = objectMapper.readValue(responseBody, ResponseModel.class);

            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
