package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.CreateTableRequest;
import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.TableDetail;
import com.amithfernando.qrseatreservation.client.model.TableDetailSummary;

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
public class TablesApi {
    private ApiClient apiClient;

    public TablesApi() {
        this(new ApiClient());
    }

    @Autowired
    public TablesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Check if table can be modified
     * Checks if a table has any active reservations
     * <p><b>200</b> - Successfully checked table status
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * @param id The id parameter
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec canModifyTableRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling canModifyTable", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
        return apiClient.invokeAPI("/api/tables/{id}/can-modify", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Check if table can be modified
     * Checks if a table has any active reservations
     * <p><b>200</b> - Successfully checked table status
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * @param id The id parameter
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Boolean> canModifyTable(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return canModifyTableRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Check if table can be modified
     * Checks if a table has any active reservations
     * <p><b>200</b> - Successfully checked table status
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * @param id The id parameter
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Boolean>> canModifyTableWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return canModifyTableRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Check if table can be modified
     * Checks if a table has any active reservations
     * <p><b>200</b> - Successfully checked table status
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec canModifyTableWithResponseSpec(Long id) throws WebClientResponseException {
        return canModifyTableRequestCreation(id);
    }
    /**
     * Create custom table
     * Creates a table with specified available and unavailable seats
     * <p><b>201</b> - Table created successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>409</b> - Table already exists - TABLE_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createTableRequest The createTableRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createTableRequestCreation(CreateTableRequest createTableRequest) throws WebClientResponseException {
        Object postBody = createTableRequest;
        // verify the required parameter 'createTableRequest' is set
        if (createTableRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'createTableRequest' when calling createTable", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
        return apiClient.invokeAPI("/api/tables", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Create custom table
     * Creates a table with specified available and unavailable seats
     * <p><b>201</b> - Table created successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>409</b> - Table already exists - TABLE_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createTableRequest The createTableRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createTable(CreateTableRequest createTableRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createTableRequestCreation(createTableRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Create custom table
     * Creates a table with specified available and unavailable seats
     * <p><b>201</b> - Table created successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>409</b> - Table already exists - TABLE_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createTableRequest The createTableRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createTableWithHttpInfo(CreateTableRequest createTableRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createTableRequestCreation(createTableRequest).toEntity(localVarReturnType);
    }

    /**
     * Create custom table
     * Creates a table with specified available and unavailable seats
     * <p><b>201</b> - Table created successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>409</b> - Table already exists - TABLE_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createTableRequest The createTableRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createTableWithResponseSpec(CreateTableRequest createTableRequest) throws WebClientResponseException {
        return createTableRequestCreation(createTableRequest);
    }
    /**
     * Delete table
     * Deletes a table and its associated seats. Cannot delete tables with active reservations.
     * <p><b>204</b> - Table deleted successfully
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table has active reservations - TABLE_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteTableRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling deleteTable", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
        return apiClient.invokeAPI("/api/tables/{id}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete table
     * Deletes a table and its associated seats. Cannot delete tables with active reservations.
     * <p><b>204</b> - Table deleted successfully
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table has active reservations - TABLE_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteTable(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteTableRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Delete table
     * Deletes a table and its associated seats. Cannot delete tables with active reservations.
     * <p><b>204</b> - Table deleted successfully
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table has active reservations - TABLE_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteTableWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteTableRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete table
     * Deletes a table and its associated seats. Cannot delete tables with active reservations.
     * <p><b>204</b> - Table deleted successfully
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table has active reservations - TABLE_HAS_RESERVATIONS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteTableWithResponseSpec(Long id) throws WebClientResponseException {
        return deleteTableRequestCreation(id);
    }
    /**
     * Get all tables
     * Retrieves all tables with their seat details
     * <p><b>200</b> - Successfully retrieved tables
     * <p><b>500</b> - Internal server error
     * @return List&lt;TableDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllTablesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return apiClient.invokeAPI("/api/tables", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all tables
     * Retrieves all tables with their seat details
     * <p><b>200</b> - Successfully retrieved tables
     * <p><b>500</b> - Internal server error
     * @return List&lt;TableDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<TableDetail> getAllTables() throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return getAllTablesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all tables
     * Retrieves all tables with their seat details
     * <p><b>200</b> - Successfully retrieved tables
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;List&lt;TableDetail&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<TableDetail>>> getAllTablesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return getAllTablesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all tables
     * Retrieves all tables with their seat details
     * <p><b>200</b> - Successfully retrieved tables
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllTablesWithResponseSpec() throws WebClientResponseException {
        return getAllTablesRequestCreation();
    }
    /**
     * Get table by ID
     * Retrieves a specific table by its ID
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return TableDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTableByIdRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getTableById", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return apiClient.invokeAPI("/api/tables/{id}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get table by ID
     * Retrieves a specific table by its ID
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return TableDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TableDetail> getTableById(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return getTableByIdRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get table by ID
     * Retrieves a specific table by its ID
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;TableDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TableDetail>> getTableByIdWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return getTableByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get table by ID
     * Retrieves a specific table by its ID
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTableByIdWithResponseSpec(Long id) throws WebClientResponseException {
        return getTableByIdRequestCreation(id);
    }
    /**
     * Get table by name
     * Retrieves a specific table by its name
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return TableDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTableByNameRequestCreation(String name) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getTableByName", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return apiClient.invokeAPI("/api/tables/search", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get table by name
     * Retrieves a specific table by its name
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return TableDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TableDetail> getTableByName(String name) throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return getTableByNameRequestCreation(name).bodyToMono(localVarReturnType);
    }

    /**
     * Get table by name
     * Retrieves a specific table by its name
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return ResponseEntity&lt;TableDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TableDetail>> getTableByNameWithHttpInfo(String name) throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return getTableByNameRequestCreation(name).toEntity(localVarReturnType);
    }

    /**
     * Get table by name
     * Retrieves a specific table by its name
     * <p><b>200</b> - Successfully retrieved table
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param name The name parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTableByNameWithResponseSpec(String name) throws WebClientResponseException {
        return getTableByNameRequestCreation(name);
    }
    /**
     * Get table summary
     * Retrieves summary statistics of all tables
     * <p><b>200</b> - Successfully retrieved summary
     * <p><b>500</b> - Internal server error
     * @return TableDetailSummary
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTableSummaryRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<TableDetailSummary> localVarReturnType = new ParameterizedTypeReference<TableDetailSummary>() {};
        return apiClient.invokeAPI("/api/tables/summary", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get table summary
     * Retrieves summary statistics of all tables
     * <p><b>200</b> - Successfully retrieved summary
     * <p><b>500</b> - Internal server error
     * @return TableDetailSummary
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TableDetailSummary> getTableSummary() throws WebClientResponseException {
        ParameterizedTypeReference<TableDetailSummary> localVarReturnType = new ParameterizedTypeReference<TableDetailSummary>() {};
        return getTableSummaryRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get table summary
     * Retrieves summary statistics of all tables
     * <p><b>200</b> - Successfully retrieved summary
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;TableDetailSummary&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TableDetailSummary>> getTableSummaryWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<TableDetailSummary> localVarReturnType = new ParameterizedTypeReference<TableDetailSummary>() {};
        return getTableSummaryRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get table summary
     * Retrieves summary statistics of all tables
     * <p><b>200</b> - Successfully retrieved summary
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTableSummaryWithResponseSpec() throws WebClientResponseException {
        return getTableSummaryRequestCreation();
    }
    /**
     * Update table
     * Updates an existing table&#39;s details. Cannot update tables with active reservations.
     * <p><b>200</b> - Table updated successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table name already exists (TABLE_ALREADY_EXISTS) or table has active reservations (TABLE_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createTableRequest The createTableRequest parameter
     * @return TableDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateTableRequestCreation(Long id, CreateTableRequest createTableRequest) throws WebClientResponseException {
        Object postBody = createTableRequest;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling updateTable", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'createTableRequest' is set
        if (createTableRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'createTableRequest' when calling updateTable", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return apiClient.invokeAPI("/api/tables/{id}", HttpMethod.PUT, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update table
     * Updates an existing table&#39;s details. Cannot update tables with active reservations.
     * <p><b>200</b> - Table updated successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table name already exists (TABLE_ALREADY_EXISTS) or table has active reservations (TABLE_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createTableRequest The createTableRequest parameter
     * @return TableDetail
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<TableDetail> updateTable(Long id, CreateTableRequest createTableRequest) throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return updateTableRequestCreation(id, createTableRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Update table
     * Updates an existing table&#39;s details. Cannot update tables with active reservations.
     * <p><b>200</b> - Table updated successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table name already exists (TABLE_ALREADY_EXISTS) or table has active reservations (TABLE_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createTableRequest The createTableRequest parameter
     * @return ResponseEntity&lt;TableDetail&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<TableDetail>> updateTableWithHttpInfo(Long id, CreateTableRequest createTableRequest) throws WebClientResponseException {
        ParameterizedTypeReference<TableDetail> localVarReturnType = new ParameterizedTypeReference<TableDetail>() {};
        return updateTableRequestCreation(id, createTableRequest).toEntity(localVarReturnType);
    }

    /**
     * Update table
     * Updates an existing table&#39;s details. Cannot update tables with active reservations.
     * <p><b>200</b> - Table updated successfully
     * <p><b>400</b> - Invalid table data
     * <p><b>404</b> - Table not found - TABLE_NOT_FOUND
     * <p><b>409</b> - Table name already exists (TABLE_ALREADY_EXISTS) or table has active reservations (TABLE_HAS_RESERVATIONS)
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param createTableRequest The createTableRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateTableWithResponseSpec(Long id, CreateTableRequest createTableRequest) throws WebClientResponseException {
        return updateTableRequestCreation(id, createTableRequest);
    }
}
