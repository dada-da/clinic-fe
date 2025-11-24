package com.clinic.ui.service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    // ==================== Patient APIs ====================

    public static String getPatients() throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/patients");
    }

    public static String searchPatients(String name) throws IOException, InterruptedException {
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return sendGetRequest(BASE_URL + "/patients/search?name=" + encodedName);
    }

    public static String getPatientById(Integer id) throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/patients/" + id);
    }

    public static String getPatientBySocialId(String socialId) throws IOException, InterruptedException {
        String encodedSocialId = URLEncoder.encode(socialId, StandardCharsets.UTF_8);
        return sendGetRequest(BASE_URL + "/patients/social/" + encodedSocialId);
    }

    public static String createPatient(String jsonBody) throws IOException, InterruptedException {
        return sendPostRequest(BASE_URL + "/patients", jsonBody);
    }

    public static String updatePatient(Integer id, String jsonBody) throws IOException, InterruptedException {
        return sendPutRequest(BASE_URL + "/patients/" + id, jsonBody);
    }

    public static void deletePatient(Integer id) throws IOException, InterruptedException {
        sendDeleteRequest(BASE_URL + "/patients/" + id);
    }

    // ==================== Doctor APIs ====================

    public static String getDoctors() throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/doctors");
    }

    public static String getDoctorById(Integer id) throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/doctors/" + id);
    }

    // ==================== Appointment APIs ====================

    public static String getAppointments() throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/appointments");
    }

    public static String getAppointmentById(Integer id) throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/appointments/" + id);
    }

    public static String createAppointment(String jsonBody) throws IOException, InterruptedException {
        return sendPostRequest(BASE_URL + "/appointments", jsonBody);
    }

    public static String completeAppointment(Integer id) throws IOException, InterruptedException {
        return sendPutRequest(BASE_URL + "/appointments/" + id + "/complete", "");
    }

    // ==================== Medical Record APIs ====================

    public static String getMedicalRecordByAppointmentId(Integer appointmentId) throws IOException, InterruptedException {
        return sendGetRequest(BASE_URL + "/medical-records/appointment/" + appointmentId);
    }

    public static String createMedicalRecord(String jsonBody) throws IOException, InterruptedException {
        return sendPostRequest(BASE_URL + "/medical-records", jsonBody);
    }

    // ==================== Helper Methods ====================

    private static String sendGetRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new IOException("Resource not found: " + url);
            } else if (response.statusCode() != 200) {
                throw new IOException("Request failed. Status: " + response.statusCode() + ", Body: " + response.body());
            }

            return response.body();
        } catch (ConnectException e) {
            throw new IOException("Cannot connect to REST API. Is the server running?", e);
        }
    }

    private static String sendPostRequest(String url, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Request failed. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        return response.body();
    }

    private static String sendPutRequest(String url, String jsonBody) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher body = jsonBody.isEmpty() ?
                HttpRequest.BodyPublishers.noBody() :
                HttpRequest.BodyPublishers.ofString(jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(body)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new IOException("Resource not found");
        } else if (response.statusCode() != 200) {
            throw new IOException("Request failed. Status: " + response.statusCode());
        }

        return response.body();
    }

    private static void sendDeleteRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new IOException("Resource not found");
        } else if (response.statusCode() != 204) {
            throw new IOException("Request failed. Status: " + response.statusCode());
        }
    }
}
