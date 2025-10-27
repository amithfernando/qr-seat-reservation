package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.CheckInRequest;
import com.amithfernando.qrseatreservation.client.model.CheckInResponse;
import com.amithfernando.qrseatreservation.client.model.CheckInStats;
import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.TicketDetailsResponse;

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
public class CheckInApi {
    private ApiClient apiClient;

    public CheckInApi() {
        this(new ApiClient());
    }

    @Autowired
    public CheckInApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Check in ticket
     * Checks in a ticket. Only allowed for PAID reservations that haven&#39;t been checked in yet.
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param checkInRequest The checkInRequest parameter
     * @return CheckInResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec checkInTicketRequestCreation(CheckInRequest checkInRequest) throws WebClientResponseException {
        Object postBody = checkInRequest;
        // verify the required parameter 'checkInRequest' is set
        if (checkInRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'checkInRequest' when calling checkInTicket", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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

        ParameterizedTypeReference<CheckInResponse> localVarReturnType = new ParameterizedTypeReference<CheckInResponse>() {};
        return apiClient.invokeAPI("/api/checkin", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Check in ticket
     * Checks in a ticket. Only allowed for PAID reservations that haven&#39;t been checked in yet.
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param checkInRequest The checkInRequest parameter
     * @return CheckInResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<CheckInResponse> checkInTicket(CheckInRequest checkInRequest) throws WebClientResponseException {
        ParameterizedTypeReference<CheckInResponse> localVarReturnType = new ParameterizedTypeReference<CheckInResponse>() {};
        return checkInTicketRequestCreation(checkInRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Check in ticket
     * Checks in a ticket. Only allowed for PAID reservations that haven&#39;t been checked in yet.
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param checkInRequest The checkInRequest parameter
     * @return ResponseEntity&lt;CheckInResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<CheckInResponse>> checkInTicketWithHttpInfo(CheckInRequest checkInRequest) throws WebClientResponseException {
        ParameterizedTypeReference<CheckInResponse> localVarReturnType = new ParameterizedTypeReference<CheckInResponse>() {};
        return checkInTicketRequestCreation(checkInRequest).toEntity(localVarReturnType);
    }

    /**
     * Check in ticket
     * Checks in a ticket. Only allowed for PAID reservations that haven&#39;t been checked in yet.
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>400</b> - Invalid request data
     * <p><b>500</b> - Internal server error
     * @param checkInRequest The checkInRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec checkInTicketWithResponseSpec(CheckInRequest checkInRequest) throws WebClientResponseException {
        return checkInTicketRequestCreation(checkInRequest);
    }
    /**
     * Check in ticket by number
     * Simplified endpoint to check in a ticket using path parameter
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return CheckInResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec checkInTicketByNumberRequestCreation(String ticketNo) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'ticketNo' is set
        if (ticketNo == null) {
            throw new WebClientResponseException("Missing the required parameter 'ticketNo' when calling checkInTicketByNumber", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<CheckInResponse> localVarReturnType = new ParameterizedTypeReference<CheckInResponse>() {};
        return apiClient.invokeAPI("/api/checkin/{ticketNo}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Check in ticket by number
     * Simplified endpoint to check in a ticket using path parameter
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return CheckInResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<CheckInResponse> checkInTicketByNumber(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<CheckInResponse> localVarReturnType = new ParameterizedTypeReference<CheckInResponse>() {};
        return checkInTicketByNumberRequestCreation(ticketNo).bodyToMono(localVarReturnType);
    }

    /**
     * Check in ticket by number
     * Simplified endpoint to check in a ticket using path parameter
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseEntity&lt;CheckInResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<CheckInResponse>> checkInTicketByNumberWithHttpInfo(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<CheckInResponse> localVarReturnType = new ParameterizedTypeReference<CheckInResponse>() {};
        return checkInTicketByNumberRequestCreation(ticketNo).toEntity(localVarReturnType);
    }

    /**
     * Check in ticket by number
     * Simplified endpoint to check in a ticket using path parameter
     * <p><b>200</b> - Check-in processed (check success field for result)
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec checkInTicketByNumberWithResponseSpec(String ticketNo) throws WebClientResponseException {
        return checkInTicketByNumberRequestCreation(ticketNo);
    }
    /**
     * Get check-in statistics
     * Retrieves overall check-in statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return CheckInStats
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getCheckInStatsRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<CheckInStats> localVarReturnType = new ParameterizedTypeReference<CheckInStats>() {};
        return apiClient.invokeAPI("/api/checkin/stats", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get check-in statistics
     * Retrieves overall check-in statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return CheckInStats
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<CheckInStats> getCheckInStats() throws WebClientResponseException {
        ParameterizedTypeReference<CheckInStats> localVarReturnType = new ParameterizedTypeReference<CheckInStats>() {};
        return getCheckInStatsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get check-in statistics
     * Retrieves overall check-in statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;CheckInStats&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<CheckInStats>> getCheckInStatsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<CheckInStats> localVarReturnType = new ParameterizedTypeReference<CheckInStats>() {};
        return getCheckInStatsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get check-in statistics
     * Retrieves overall check-in statistics
     * <p><b>200</b> - Successfully retrieved statistics
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getCheckInStatsWithResponseSpec() throws WebClientResponseException {
        return getCheckInStatsRequestCreation();
    }
    /**
     * Get ticket details
     * Retrieves detailed information about a ticket including whether it can be checked in
     * <p><b>200</b> - Successfully retrieved ticket details
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return TicketDetailsResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTicketDetailsRequestCreation(String ticketNo) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'ticketNo' is set
        if (ticketNo == null) {
            throw new WebClientResponseException("Missing the required parameter 'ticketNo' when calling getTicketDetails", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<TicketDetailsResponse> localVarReturnType = new ParameterizedTypeReference<TicketDetailsResponse>() {};
        return apiClient.invokeAPI("/api/checkin/ticket/{ticketNo}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get ticket details
     * Retrieves detailed information about a ticket including whether it can be checked in
     * <p><b>200</b> - Successfully retrieved ticket details
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return TicketDetailsResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TicketDetailsResponse> getTicketDetails(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<TicketDetailsResponse> localVarReturnType = new ParameterizedTypeReference<TicketDetailsResponse>() {};
        return getTicketDetailsRequestCreation(ticketNo).bodyToMono(localVarReturnType);
    }

    /**
     * Get ticket details
     * Retrieves detailed information about a ticket including whether it can be checked in
     * <p><b>200</b> - Successfully retrieved ticket details
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseEntity&lt;TicketDetailsResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TicketDetailsResponse>> getTicketDetailsWithHttpInfo(String ticketNo) throws WebClientResponseException {
        ParameterizedTypeReference<TicketDetailsResponse> localVarReturnType = new ParameterizedTypeReference<TicketDetailsResponse>() {};
        return getTicketDetailsRequestCreation(ticketNo).toEntity(localVarReturnType);
    }

    /**
     * Get ticket details
     * Retrieves detailed information about a ticket including whether it can be checked in
     * <p><b>200</b> - Successfully retrieved ticket details
     * <p><b>404</b> - Ticket not found - TICKET_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param ticketNo The ticketNo parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTicketDetailsWithResponseSpec(String ticketNo) throws WebClientResponseException {
        return getTicketDetailsRequestCreation(ticketNo);
    }
}
