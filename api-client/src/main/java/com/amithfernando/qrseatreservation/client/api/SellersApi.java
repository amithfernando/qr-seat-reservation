package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.CreateSellerRequest;
import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.SellerDetail;

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
public class SellersApi {
    private ApiClient apiClient;

    public SellersApi() {
        this(new ApiClient());
    }

    @Autowired
    public SellersApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Check if seller can be modified
     * Checks if a seller has any active reservations
     * <p><b>200</b> - Successfully checked seller status
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * @param id The id parameter
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec canModifySellerRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling canModifySeller", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return apiClient.invokeAPI("/api/sellers/{id}/can-modify", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Check if seller can be modified
     * Checks if a seller has any active reservations
     * <p><b>200</b> - Successfully checked seller status
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * @param id The id parameter
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Boolean> canModifySeller(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return canModifySellerRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Check if seller can be modified
     * Checks if a seller has any active reservations
     * <p><b>200</b> - Successfully checked seller status
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * @param id The id parameter
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Boolean>> canModifySellerWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return canModifySellerRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Check if seller can be modified
     * Checks if a seller has any active reservations
     * <p><b>200</b> - Successfully checked seller status
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec canModifySellerWithResponseSpec(Long id) throws WebClientResponseException {
        return canModifySellerRequestCreation(id);
    }
    /**
     * Create custom seller
     * Creates a seller with individual parameters
     * <p><b>201</b> - Seller created successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>409</b> - Seller already exists - SELLER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createSellerRequest The createSellerRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createCustomSellerRequestCreation(CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        Object postBody = createSellerRequest;
        // verify the required parameter 'createSellerRequest' is set
        if (createSellerRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'createSellerRequest' when calling createCustomSeller", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/api/sellers", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Create custom seller
     * Creates a seller with individual parameters
     * <p><b>201</b> - Seller created successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>409</b> - Seller already exists - SELLER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createSellerRequest The createSellerRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createCustomSeller(CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createCustomSellerRequestCreation(createSellerRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Create custom seller
     * Creates a seller with individual parameters
     * <p><b>201</b> - Seller created successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>409</b> - Seller already exists - SELLER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createSellerRequest The createSellerRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createCustomSellerWithHttpInfo(CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createCustomSellerRequestCreation(createSellerRequest).toEntity(localVarReturnType);
    }

    /**
     * Create custom seller
     * Creates a seller with individual parameters
     * <p><b>201</b> - Seller created successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>409</b> - Seller already exists - SELLER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createSellerRequest The createSellerRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createCustomSellerWithResponseSpec(CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        return createCustomSellerRequestCreation(createSellerRequest);
    }
    /**
     * Delete seller
     * Deletes a seller. Cannot delete sellers with active reservations.
     * <p><b>204</b> - Seller deleted successfully
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller has active reservations - SELLER_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteSellerRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling deleteSeller", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
        return apiClient.invokeAPI("/api/sellers/{id}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete seller
     * Deletes a seller. Cannot delete sellers with active reservations.
     * <p><b>204</b> - Seller deleted successfully
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller has active reservations - SELLER_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteSeller(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteSellerRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Delete seller
     * Deletes a seller. Cannot delete sellers with active reservations.
     * <p><b>204</b> - Seller deleted successfully
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller has active reservations - SELLER_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteSellerWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteSellerRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete seller
     * Deletes a seller. Cannot delete sellers with active reservations.
     * <p><b>204</b> - Seller deleted successfully
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller has active reservations - SELLER_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteSellerWithResponseSpec(Long id) throws WebClientResponseException {
        return deleteSellerRequestCreation(id);
    }
    /**
     * Get all sellers
     * Retrieves all registered sellers
     * <p><b>200</b> - Successfully retrieved sellers
     * <p><b>500</b> - Internal server error
     * @return List&lt;SellerDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllSellersRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return apiClient.invokeAPI("/api/sellers", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all sellers
     * Retrieves all registered sellers
     * <p><b>200</b> - Successfully retrieved sellers
     * <p><b>500</b> - Internal server error
     * @return List&lt;SellerDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<SellerDetail> getAllSellers() throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return getAllSellersRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all sellers
     * Retrieves all registered sellers
     * <p><b>200</b> - Successfully retrieved sellers
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;List&lt;SellerDetail&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<SellerDetail>>> getAllSellersWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return getAllSellersRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all sellers
     * Retrieves all registered sellers
     * <p><b>200</b> - Successfully retrieved sellers
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllSellersWithResponseSpec() throws WebClientResponseException {
        return getAllSellersRequestCreation();
    }
    /**
     * Get seller by ID
     * Retrieves a specific seller by their ID
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return SellerDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSellerByIdRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getSellerById", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return apiClient.invokeAPI("/api/sellers/{id}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get seller by ID
     * Retrieves a specific seller by their ID
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return SellerDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SellerDetail> getSellerById(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return getSellerByIdRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get seller by ID
     * Retrieves a specific seller by their ID
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;SellerDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SellerDetail>> getSellerByIdWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return getSellerByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get seller by ID
     * Retrieves a specific seller by their ID
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSellerByIdWithResponseSpec(Long id) throws WebClientResponseException {
        return getSellerByIdRequestCreation(id);
    }
    /**
     * Get seller by name
     * Retrieves a specific seller by their name
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return SellerDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSellerByNameRequestCreation(String name) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getSellerByName", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        
        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return apiClient.invokeAPI("/api/sellers/search", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get seller by name
     * Retrieves a specific seller by their name
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return SellerDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SellerDetail> getSellerByName(String name) throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return getSellerByNameRequestCreation(name).bodyToMono(localVarReturnType);
    }

    /**
     * Get seller by name
     * Retrieves a specific seller by their name
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return ResponseEntity&lt;SellerDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SellerDetail>> getSellerByNameWithHttpInfo(String name) throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return getSellerByNameRequestCreation(name).toEntity(localVarReturnType);
    }

    /**
     * Get seller by name
     * Retrieves a specific seller by their name
     * <p><b>200</b> - Successfully retrieved seller
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSellerByNameWithResponseSpec(String name) throws WebClientResponseException {
        return getSellerByNameRequestCreation(name);
    }
    /**
     * Update seller
     * Updates an existing seller&#39;s details. Cannot update sellers with active reservations.
     * <p><b>200</b> - Seller updated successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller name already exists (SELLER_ALREADY_EXISTS) or seller has active reservations (SELLER_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createSellerRequest The createSellerRequest parameter
     * @return SellerDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateSellerRequestCreation(Long id, CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        Object postBody = createSellerRequest;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling updateSeller", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'createSellerRequest' is set
        if (createSellerRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'createSellerRequest' when calling updateSeller", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return apiClient.invokeAPI("/api/sellers/{id}", HttpMethod.PUT, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update seller
     * Updates an existing seller&#39;s details. Cannot update sellers with active reservations.
     * <p><b>200</b> - Seller updated successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller name already exists (SELLER_ALREADY_EXISTS) or seller has active reservations (SELLER_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createSellerRequest The createSellerRequest parameter
     * @return SellerDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SellerDetail> updateSeller(Long id, CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return updateSellerRequestCreation(id, createSellerRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Update seller
     * Updates an existing seller&#39;s details. Cannot update sellers with active reservations.
     * <p><b>200</b> - Seller updated successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller name already exists (SELLER_ALREADY_EXISTS) or seller has active reservations (SELLER_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createSellerRequest The createSellerRequest parameter
     * @return ResponseEntity&lt;SellerDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SellerDetail>> updateSellerWithHttpInfo(Long id, CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        ParameterizedTypeReference<SellerDetail> localVarReturnType = new ParameterizedTypeReference<SellerDetail>() {};
        return updateSellerRequestCreation(id, createSellerRequest).toEntity(localVarReturnType);
    }

    /**
     * Update seller
     * Updates an existing seller&#39;s details. Cannot update sellers with active reservations.
     * <p><b>200</b> - Seller updated successfully
     * <p><b>400</b> - Invalid seller data
     * <p><b>404</b> - Seller not found - SELLER_NOT_FOUND
     * <p><b>409</b> - Seller name already exists (SELLER_ALREADY_EXISTS) or seller has active reservations (SELLER_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createSellerRequest The createSellerRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateSellerWithResponseSpec(Long id, CreateSellerRequest createSellerRequest) throws WebClientResponseException {
        return updateSellerRequestCreation(id, createSellerRequest);
    }
}
