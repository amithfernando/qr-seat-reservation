package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.GenerateTicketsRequest;
import com.amithfernando.qrseatreservation.client.model.TicketResponse;
import com.amithfernando.qrseatreservation.client.model.TicketStatsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class TicketsApi {
    private ApiClient apiClient;

    public TicketsApi() {
        this(new ApiClient());
    }

    @Autowired
    public TicketsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Delete all tickets
     * Deletes all tickets from the system (use with caution)
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteAllTicketsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return apiClient.invokeAPI("/api/tickets/all", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete all tickets
     * Deletes all tickets from the system (use with caution)
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Map<String, Object>> deleteAllTickets() throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return deleteAllTicketsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Delete all tickets
     * Deletes all tickets from the system (use with caution)
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;Map&lt;String, Object&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Map<String, Object>>> deleteAllTicketsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return deleteAllTicketsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Delete all tickets
     * Deletes all tickets from the system (use with caution)
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteAllTicketsWithResponseSpec() throws WebClientResponseException {
        return deleteAllTicketsRequestCreation();
    }
    /**
     * Delete tickets by status
     * Deletes all tickets with a specific status
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteTicketsByStatusRequestCreation(String status) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'status' is set
        if (status == null) {
            throw new WebClientResponseException("Missing the required parameter 'status' when calling deleteTicketsByStatus", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("status", status);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return apiClient.invokeAPI("/api/tickets/status/{status}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete tickets by status
     * Deletes all tickets with a specific status
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Map<String, Object>> deleteTicketsByStatus(String status) throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return deleteTicketsByStatusRequestCreation(status).bodyToMono(localVarReturnType);
    }

    /**
     * Delete tickets by status
     * Deletes all tickets with a specific status
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return ResponseEntity&lt;Map&lt;String, Object&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Map<String, Object>>> deleteTicketsByStatusWithHttpInfo(String status) throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return deleteTicketsByStatusRequestCreation(status).toEntity(localVarReturnType);
    }

    /**
     * Delete tickets by status
     * Deletes all tickets with a specific status
     * <p><b>200</b> - Tickets deleted successfully
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteTicketsByStatusWithResponseSpec(String status) throws WebClientResponseException {
        return deleteTicketsByStatusRequestCreation(status);
    }
    /**
     * Download ticket image
     * Downloads the QR ticket image
     * <p><b>200</b> - Successfully retrieved image
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return byte[]
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec downloadTicketImageRequestCreation(String ticketNo) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'ticketNo' is set
        if (ticketNo == null) {
            throw new WebClientResponseException("Missing the required parameter 'ticketNo' when calling downloadTicketImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("ticketNo", ticketNo);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<byte[]> localVarReturnType = new ParameterizedTypeReference<byte[]>() {};
        return apiClient.invokeAPI("/api/tickets/{ticketNo}/image", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Download ticket image
     * Downloads the QR ticket image
     * <p><b>200</b> - Successfully retrieved image
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return byte[]
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<byte[]> downloadTicketImage(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<byte[]> localVarReturnType = new ParameterizedTypeReference<byte[]>() {};
        return downloadTicketImageRequestCreation(ticketNo).bodyToMono(localVarReturnType);
    }

    /**
     * Download ticket image
     * Downloads the QR ticket image
     * <p><b>200</b> - Successfully retrieved image
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseEntity&lt;byte[]&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<byte[]>> downloadTicketImageWithHttpInfo(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<byte[]> localVarReturnType = new ParameterizedTypeReference<byte[]>() {};
        return downloadTicketImageRequestCreation(ticketNo).toEntity(localVarReturnType);
    }

    /**
     * Download ticket image
     * Downloads the QR ticket image
     * <p><b>200</b> - Successfully retrieved image
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec downloadTicketImageWithResponseSpec(String ticketNo) throws WebClientResponseException {
        return downloadTicketImageRequestCreation(ticketNo);
    }
    /**
     * Generate tickets
     * Generates new tickets. Uses count from request or settings if not provided.
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param generateTicketsRequest The generateTicketsRequest parameter
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec generateTicketsRequestCreation(GenerateTicketsRequest generateTicketsRequest) throws WebClientResponseException {
        Object postBody = generateTicketsRequest;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return apiClient.invokeAPI("/api/tickets/generate", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Generate tickets
     * Generates new tickets. Uses count from request or settings if not provided.
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param generateTicketsRequest The generateTicketsRequest parameter
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Map<String, Object>> generateTickets(GenerateTicketsRequest generateTicketsRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return generateTicketsRequestCreation(generateTicketsRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Generate tickets
     * Generates new tickets. Uses count from request or settings if not provided.
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param generateTicketsRequest The generateTicketsRequest parameter
     * @return ResponseEntity&lt;Map&lt;String, Object&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Map<String, Object>>> generateTicketsWithHttpInfo(GenerateTicketsRequest generateTicketsRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return generateTicketsRequestCreation(generateTicketsRequest).toEntity(localVarReturnType);
    }

    /**
     * Generate tickets
     * Generates new tickets. Uses count from request or settings if not provided.
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param generateTicketsRequest The generateTicketsRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec generateTicketsWithResponseSpec(GenerateTicketsRequest generateTicketsRequest) throws WebClientResponseException {
        return generateTicketsRequestCreation(generateTicketsRequest);
    }
    /**
     * Generate tickets (simple)
     * Generates tickets using settings configuration
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>500</b> - Internal server error
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec generateTicketsDefaultRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return apiClient.invokeAPI("/api/tickets/generate/default", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Generate tickets (simple)
     * Generates tickets using settings configuration
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>500</b> - Internal server error
     * @return Map&lt;String, Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Map<String, Object>> generateTicketsDefault() throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return generateTicketsDefaultRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Generate tickets (simple)
     * Generates tickets using settings configuration
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;Map&lt;String, Object&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Map<String, Object>>> generateTicketsDefaultWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Map<String, Object>> localVarReturnType = new ParameterizedTypeReference<Map<String, Object>>() {};
        return generateTicketsDefaultRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Generate tickets (simple)
     * Generates tickets using settings configuration
     * <p><b>201</b> - Tickets generated successfully
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec generateTicketsDefaultWithResponseSpec() throws WebClientResponseException {
        return generateTicketsDefaultRequestCreation();
    }
    /**
     * Get all tickets
     * Retrieves all tickets with metadata (image data excluded)
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @return List&lt;TicketResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllTicketsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return apiClient.invokeAPI("/api/tickets", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all tickets
     * Retrieves all tickets with metadata (image data excluded)
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @return List&lt;TicketResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<TicketResponse> getAllTickets() throws WebClientResponseException {
        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return getAllTicketsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all tickets
     * Retrieves all tickets with metadata (image data excluded)
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;List&lt;TicketResponse&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<TicketResponse>>> getAllTicketsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return getAllTicketsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all tickets
     * Retrieves all tickets with metadata (image data excluded)
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllTicketsWithResponseSpec() throws WebClientResponseException {
        return getAllTicketsRequestCreation();
    }
    /**
     * Get ticket by number
     * Retrieves ticket metadata by ticket number
     * <p><b>200</b> - Successfully retrieved ticket
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return TicketResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTicketByNumberRequestCreation(String ticketNo) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'ticketNo' is set
        if (ticketNo == null) {
            throw new WebClientResponseException("Missing the required parameter 'ticketNo' when calling getTicketByNumber", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("ticketNo", ticketNo);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return apiClient.invokeAPI("/api/tickets/{ticketNo}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get ticket by number
     * Retrieves ticket metadata by ticket number
     * <p><b>200</b> - Successfully retrieved ticket
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return TicketResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TicketResponse> getTicketByNumber(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return getTicketByNumberRequestCreation(ticketNo).bodyToMono(localVarReturnType);
    }

    /**
     * Get ticket by number
     * Retrieves ticket metadata by ticket number
     * <p><b>200</b> - Successfully retrieved ticket
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseEntity&lt;TicketResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TicketResponse>> getTicketByNumberWithHttpInfo(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return getTicketByNumberRequestCreation(ticketNo).toEntity(localVarReturnType);
    }

    /**
     * Get ticket by number
     * Retrieves ticket metadata by ticket number
     * <p><b>200</b> - Successfully retrieved ticket
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTicketByNumberWithResponseSpec(String ticketNo) throws WebClientResponseException {
        return getTicketByNumberRequestCreation(ticketNo);
    }
    /**
     * Get ticket statistics
     * Retrieves overall ticket statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return TicketStatsResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTicketStatsRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TicketStatsResponse> localVarReturnType = new ParameterizedTypeReference<TicketStatsResponse>() {};
        return apiClient.invokeAPI("/api/tickets/stats", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get ticket statistics
     * Retrieves overall ticket statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return TicketStatsResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TicketStatsResponse> getTicketStats() throws WebClientResponseException {
        ParameterizedTypeReference<TicketStatsResponse> localVarReturnType = new ParameterizedTypeReference<TicketStatsResponse>() {};
        return getTicketStatsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get ticket statistics
     * Retrieves overall ticket statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;TicketStatsResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TicketStatsResponse>> getTicketStatsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<TicketStatsResponse> localVarReturnType = new ParameterizedTypeReference<TicketStatsResponse>() {};
        return getTicketStatsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get ticket statistics
     * Retrieves overall ticket statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTicketStatsWithResponseSpec() throws WebClientResponseException {
        return getTicketStatsRequestCreation();
    }
    /**
     * Get tickets by status
     * Retrieves all tickets with a specific status
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return List&lt;TicketResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTicketsByStatusRequestCreation(String status) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'status' is set
        if (status == null) {
            throw new WebClientResponseException("Missing the required parameter 'status' when calling getTicketsByStatus", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("status", status);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return apiClient.invokeAPI("/api/tickets/status/{status}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get tickets by status
     * Retrieves all tickets with a specific status
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return List&lt;TicketResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<TicketResponse> getTicketsByStatus(String status) throws WebClientResponseException {
        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return getTicketsByStatusRequestCreation(status).bodyToFlux(localVarReturnType);
    }

    /**
     * Get tickets by status
     * Retrieves all tickets with a specific status
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return ResponseEntity&lt;List&lt;TicketResponse&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<TicketResponse>>> getTicketsByStatusWithHttpInfo(String status) throws WebClientResponseException {
        ParameterizedTypeReference<TicketResponse> localVarReturnType = new ParameterizedTypeReference<TicketResponse>() {};
        return getTicketsByStatusRequestCreation(status).toEntityList(localVarReturnType);
    }

    /**
     * Get tickets by status
     * Retrieves all tickets with a specific status
     * <p><b>200</b> - Successfully retrieved tickets
     * <p><b>500</b> - Internal server error
     * @param status The status parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTicketsByStatusWithResponseSpec(String status) throws WebClientResponseException {
        return getTicketsByStatusRequestCreation(status);
    }
}
