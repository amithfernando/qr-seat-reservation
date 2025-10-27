package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.CreateReservationRequest;
import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.ReservationResponse;

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
public class ReservationsApi {
    private ApiClient apiClient;

    public ReservationsApi() {
        this(new ApiClient());
    }

    @Autowired
    public ReservationsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Create reservation
     * Creates a new seat reservation with specified seats and ticket types
     * <p><b>201</b> - Reservation created successfully
     * <p><b>400</b> - Invalid reservation data
     * <p><b>404</b> - Seller or seat not found
     * <p><b>409</b> - Seat already reserved - SEAT_ALREADY_RESERVED
     * <p><b>500</b> - Internal server error
     * @param createReservationRequest The createReservationRequest parameter
     * @return ReservationResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createReservationRequestCreation(CreateReservationRequest createReservationRequest) throws WebClientResponseException {
        Object postBody = createReservationRequest;
        // verify the required parameter 'createReservationRequest' is set
        if (createReservationRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'createReservationRequest' when calling createReservation", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return apiClient.invokeAPI("/api/reservations", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Create reservation
     * Creates a new seat reservation with specified seats and ticket types
     * <p><b>201</b> - Reservation created successfully
     * <p><b>400</b> - Invalid reservation data
     * <p><b>404</b> - Seller or seat not found
     * <p><b>409</b> - Seat already reserved - SEAT_ALREADY_RESERVED
     * <p><b>500</b> - Internal server error
     * @param createReservationRequest The createReservationRequest parameter
     * @return ReservationResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ReservationResponse> createReservation(CreateReservationRequest createReservationRequest) throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return createReservationRequestCreation(createReservationRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Create reservation
     * Creates a new seat reservation with specified seats and ticket types
     * <p><b>201</b> - Reservation created successfully
     * <p><b>400</b> - Invalid reservation data
     * <p><b>404</b> - Seller or seat not found
     * <p><b>409</b> - Seat already reserved - SEAT_ALREADY_RESERVED
     * <p><b>500</b> - Internal server error
     * @param createReservationRequest The createReservationRequest parameter
     * @return ResponseEntity&lt;ReservationResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ReservationResponse>> createReservationWithHttpInfo(CreateReservationRequest createReservationRequest) throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return createReservationRequestCreation(createReservationRequest).toEntity(localVarReturnType);
    }

    /**
     * Create reservation
     * Creates a new seat reservation with specified seats and ticket types
     * <p><b>201</b> - Reservation created successfully
     * <p><b>400</b> - Invalid reservation data
     * <p><b>404</b> - Seller or seat not found
     * <p><b>409</b> - Seat already reserved - SEAT_ALREADY_RESERVED
     * <p><b>500</b> - Internal server error
     * @param createReservationRequest The createReservationRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createReservationWithResponseSpec(CreateReservationRequest createReservationRequest) throws WebClientResponseException {
        return createReservationRequestCreation(createReservationRequest);
    }
    /**
     * Delete reservation
     * Deletes a reservation and releases associated seats
     * <p><b>204</b> - Reservation deleted successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteReservationRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling deleteReservation", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/api/reservations/{id}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete reservation
     * Deletes a reservation and releases associated seats
     * <p><b>204</b> - Reservation deleted successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteReservation(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteReservationRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Delete reservation
     * Deletes a reservation and releases associated seats
     * <p><b>204</b> - Reservation deleted successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteReservationWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteReservationRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete reservation
     * Deletes a reservation and releases associated seats
     * <p><b>204</b> - Reservation deleted successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteReservationWithResponseSpec(Long id) throws WebClientResponseException {
        return deleteReservationRequestCreation(id);
    }
    /**
     * Get all reservations
     * Retrieves all seat reservations
     * <p><b>200</b> - Successfully retrieved reservations
     * <p><b>500</b> - Internal server error
     * @return List&lt;ReservationResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllReservationsRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return apiClient.invokeAPI("/api/reservations", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all reservations
     * Retrieves all seat reservations
     * <p><b>200</b> - Successfully retrieved reservations
     * <p><b>500</b> - Internal server error
     * @return List&lt;ReservationResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ReservationResponse> getAllReservations() throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return getAllReservationsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all reservations
     * Retrieves all seat reservations
     * <p><b>200</b> - Successfully retrieved reservations
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;List&lt;ReservationResponse&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ReservationResponse>>> getAllReservationsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return getAllReservationsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all reservations
     * Retrieves all seat reservations
     * <p><b>200</b> - Successfully retrieved reservations
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllReservationsWithResponseSpec() throws WebClientResponseException {
        return getAllReservationsRequestCreation();
    }
    /**
     * Get reservation by ID
     * Retrieves a specific reservation by its ID
     * <p><b>200</b> - Successfully retrieved reservation
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ReservationResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getReservationByIdRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getReservationById", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

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

        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return apiClient.invokeAPI("/api/reservations/{id}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get reservation by ID
     * Retrieves a specific reservation by its ID
     * <p><b>200</b> - Successfully retrieved reservation
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ReservationResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ReservationResponse> getReservationById(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return getReservationByIdRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get reservation by ID
     * Retrieves a specific reservation by its ID
     * <p><b>200</b> - Successfully retrieved reservation
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;ReservationResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ReservationResponse>> getReservationByIdWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return getReservationByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get reservation by ID
     * Retrieves a specific reservation by its ID
     * <p><b>200</b> - Successfully retrieved reservation
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getReservationByIdWithResponseSpec(Long id) throws WebClientResponseException {
        return getReservationByIdRequestCreation(id);
    }
    /**
     * Mark reservation as paid
     * Updates reservation status to PAID
     * <p><b>200</b> - Reservation marked as paid successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ReservationResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec markReservationAsPaidRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling markReservationAsPaid", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

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

        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return apiClient.invokeAPI("/api/reservations/{id}/mark-paid", HttpMethod.PATCH, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Mark reservation as paid
     * Updates reservation status to PAID
     * <p><b>200</b> - Reservation marked as paid successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ReservationResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ReservationResponse> markReservationAsPaid(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return markReservationAsPaidRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Mark reservation as paid
     * Updates reservation status to PAID
     * <p><b>200</b> - Reservation marked as paid successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;ReservationResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ReservationResponse>> markReservationAsPaidWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<ReservationResponse> localVarReturnType = new ParameterizedTypeReference<ReservationResponse>() {};
        return markReservationAsPaidRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Mark reservation as paid
     * Updates reservation status to PAID
     * <p><b>200</b> - Reservation marked as paid successfully
     * <p><b>404</b> - Reservation not found - RESERVATION_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec markReservationAsPaidWithResponseSpec(Long id) throws WebClientResponseException {
        return markReservationAsPaidRequestCreation(id);
    }
}
