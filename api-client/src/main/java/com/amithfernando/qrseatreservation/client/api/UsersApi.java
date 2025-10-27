package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.ChangePasswordRequest;
import com.amithfernando.qrseatreservation.client.model.CreateUserRequest;
import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.UpdateUserRequest;
import com.amithfernando.qrseatreservation.client.model.UserResponse;

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
public class UsersApi {
    private ApiClient apiClient;

    public UsersApi() {
        this(new ApiClient());
    }

    @Autowired
    public UsersApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Change password
     * Changes user password after verifying current password
     * <p><b>204</b> - Password changed successfully
     * <p><b>400</b> - Invalid password - INVALID_PASSWORD
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param changePasswordRequest The changePasswordRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec changePasswordRequestCreation(Long id, ChangePasswordRequest changePasswordRequest) throws WebClientResponseException {
        Object postBody = changePasswordRequest;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling changePassword", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'changePasswordRequest' is set
        if (changePasswordRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'changePasswordRequest' when calling changePassword", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/api/users/{id}/change-password", HttpMethod.PATCH, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Change password
     * Changes user password after verifying current password
     * <p><b>204</b> - Password changed successfully
     * <p><b>400</b> - Invalid password - INVALID_PASSWORD
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param changePasswordRequest The changePasswordRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> changePassword(Long id, ChangePasswordRequest changePasswordRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return changePasswordRequestCreation(id, changePasswordRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Change password
     * Changes user password after verifying current password
     * <p><b>204</b> - Password changed successfully
     * <p><b>400</b> - Invalid password - INVALID_PASSWORD
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param changePasswordRequest The changePasswordRequest parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> changePasswordWithHttpInfo(Long id, ChangePasswordRequest changePasswordRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return changePasswordRequestCreation(id, changePasswordRequest).toEntity(localVarReturnType);
    }

    /**
     * Change password
     * Changes user password after verifying current password
     * <p><b>204</b> - Password changed successfully
     * <p><b>400</b> - Invalid password - INVALID_PASSWORD
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param changePasswordRequest The changePasswordRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec changePasswordWithResponseSpec(Long id, ChangePasswordRequest changePasswordRequest) throws WebClientResponseException {
        return changePasswordRequestCreation(id, changePasswordRequest);
    }
    /**
     * Create user
     * Creates a new user account with encrypted password
     * <p><b>201</b> - User created successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createUserRequest The createUserRequest parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createUserRequestCreation(CreateUserRequest createUserRequest) throws WebClientResponseException {
        Object postBody = createUserRequest;
        // verify the required parameter 'createUserRequest' is set
        if (createUserRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'createUserRequest' when calling createUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Create user
     * Creates a new user account with encrypted password
     * <p><b>201</b> - User created successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createUserRequest The createUserRequest parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserResponse> createUser(CreateUserRequest createUserRequest) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return createUserRequestCreation(createUserRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Create user
     * Creates a new user account with encrypted password
     * <p><b>201</b> - User created successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createUserRequest The createUserRequest parameter
     * @return ResponseEntity&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserResponse>> createUserWithHttpInfo(CreateUserRequest createUserRequest) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return createUserRequestCreation(createUserRequest).toEntity(localVarReturnType);
    }

    /**
     * Create user
     * Creates a new user account with encrypted password
     * <p><b>201</b> - User created successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param createUserRequest The createUserRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createUserWithResponseSpec(CreateUserRequest createUserRequest) throws WebClientResponseException {
        return createUserRequestCreation(createUserRequest);
    }
    /**
     * Delete user
     * Permanently deletes a user account
     * <p><b>204</b> - User deleted successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling deleteUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
        return apiClient.invokeAPI("/api/users/{id}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete user
     * Permanently deletes a user account
     * <p><b>204</b> - User deleted successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteUser(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Delete user
     * Permanently deletes a user account
     * <p><b>204</b> - User deleted successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteUserWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete user
     * Permanently deletes a user account
     * <p><b>204</b> - User deleted successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserWithResponseSpec(Long id) throws WebClientResponseException {
        return deleteUserRequestCreation(id);
    }
    /**
     * Disable user
     * Disables a user account
     * <p><b>200</b> - User disabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec disableUserRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling disableUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users/{id}/disable", HttpMethod.PATCH, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Disable user
     * Disables a user account
     * <p><b>200</b> - User disabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserResponse> disableUser(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return disableUserRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Disable user
     * Disables a user account
     * <p><b>200</b> - User disabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserResponse>> disableUserWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return disableUserRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Disable user
     * Disables a user account
     * <p><b>200</b> - User disabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec disableUserWithResponseSpec(Long id) throws WebClientResponseException {
        return disableUserRequestCreation(id);
    }
    /**
     * Enable user
     * Enables a user account
     * <p><b>200</b> - User enabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec enableUserRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling enableUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users/{id}/enable", HttpMethod.PATCH, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Enable user
     * Enables a user account
     * <p><b>200</b> - User enabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserResponse> enableUser(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return enableUserRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Enable user
     * Enables a user account
     * <p><b>200</b> - User enabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserResponse>> enableUserWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return enableUserRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Enable user
     * Enables a user account
     * <p><b>200</b> - User enabled successfully
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec enableUserWithResponseSpec(Long id) throws WebClientResponseException {
        return enableUserRequestCreation(id);
    }
    /**
     * Get all users
     * Retrieves all registered users (passwords excluded)
     * <p><b>200</b> - Successfully retrieved users
     * <p><b>500</b> - Internal server error
     * @return List&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllUsersRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all users
     * Retrieves all registered users (passwords excluded)
     * <p><b>200</b> - Successfully retrieved users
     * <p><b>500</b> - Internal server error
     * @return List&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<UserResponse> getAllUsers() throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return getAllUsersRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all users
     * Retrieves all registered users (passwords excluded)
     * <p><b>200</b> - Successfully retrieved users
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;List&lt;UserResponse&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<UserResponse>>> getAllUsersWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return getAllUsersRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all users
     * Retrieves all registered users (passwords excluded)
     * <p><b>200</b> - Successfully retrieved users
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllUsersWithResponseSpec() throws WebClientResponseException {
        return getAllUsersRequestCreation();
    }
    /**
     * Get user by ID
     * Retrieves a specific user by their ID
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserByIdRequestCreation(Long id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getUserById", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users/{id}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user by ID
     * Retrieves a specific user by their ID
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserResponse> getUserById(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return getUserByIdRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Get user by ID
     * Retrieves a specific user by their ID
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseEntity&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserResponse>> getUserByIdWithHttpInfo(Long id) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return getUserByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get user by ID
     * Retrieves a specific user by their ID
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserByIdWithResponseSpec(Long id) throws WebClientResponseException {
        return getUserByIdRequestCreation(id);
    }
    /**
     * Get user by username
     * Retrieves a specific user by their username
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserByUsernameRequestCreation(String username) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'username' is set
        if (username == null) {
            throw new WebClientResponseException("Missing the required parameter 'username' when calling getUserByUsername", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("username", username);

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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users/username/{username}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user by username
     * Retrieves a specific user by their username
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserResponse> getUserByUsername(String username) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return getUserByUsernameRequestCreation(username).bodyToMono(localVarReturnType);
    }

    /**
     * Get user by username
     * Retrieves a specific user by their username
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return ResponseEntity&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserResponse>> getUserByUsernameWithHttpInfo(String username) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return getUserByUsernameRequestCreation(username).toEntity(localVarReturnType);
    }

    /**
     * Get user by username
     * Retrieves a specific user by their username
     * <p><b>200</b> - Successfully retrieved user
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserByUsernameWithResponseSpec(String username) throws WebClientResponseException {
        return getUserByUsernameRequestCreation(username);
    }
    /**
     * Update user
     * Updates user information. All fields are optional.
     * <p><b>200</b> - User updated successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param updateUserRequest The updateUserRequest parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserRequestCreation(Long id, UpdateUserRequest updateUserRequest) throws WebClientResponseException {
        Object postBody = updateUserRequest;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling updateUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'updateUserRequest' is set
        if (updateUserRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'updateUserRequest' when calling updateUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return apiClient.invokeAPI("/api/users/{id}", HttpMethod.PUT, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update user
     * Updates user information. All fields are optional.
     * <p><b>200</b> - User updated successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param updateUserRequest The updateUserRequest parameter
     * @return UserResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserResponse> updateUser(Long id, UpdateUserRequest updateUserRequest) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return updateUserRequestCreation(id, updateUserRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Update user
     * Updates user information. All fields are optional.
     * <p><b>200</b> - User updated successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param updateUserRequest The updateUserRequest parameter
     * @return ResponseEntity&lt;UserResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserResponse>> updateUserWithHttpInfo(Long id, UpdateUserRequest updateUserRequest) throws WebClientResponseException {
        ParameterizedTypeReference<UserResponse> localVarReturnType = new ParameterizedTypeReference<UserResponse>() {};
        return updateUserRequestCreation(id, updateUserRequest).toEntity(localVarReturnType);
    }

    /**
     * Update user
     * Updates user information. All fields are optional.
     * <p><b>200</b> - User updated successfully
     * <p><b>400</b> - Invalid user data
     * <p><b>404</b> - User not found - USER_NOT_FOUND
     * <p><b>409</b> - Username already exists - USER_ALREADY_EXISTS
     * <p><b>500</b> - Internal server error
     * @param id The id parameter
     * @param updateUserRequest The updateUserRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserWithResponseSpec(Long id, UpdateUserRequest updateUserRequest) throws WebClientResponseException {
        return updateUserRequestCreation(id, updateUserRequest);
    }
    /**
     * Check username availability
     * Checks if a username is already taken
     * <p><b>200</b> - Successfully checked username
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec usernameExistsRequestCreation(String username) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'username' is set
        if (username == null) {
            throw new WebClientResponseException("Missing the required parameter 'username' when calling usernameExists", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("username", username);

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
        return apiClient.invokeAPI("/api/users/exists/{username}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Check username availability
     * Checks if a username is already taken
     * <p><b>200</b> - Successfully checked username
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Boolean> usernameExists(String username) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return usernameExistsRequestCreation(username).bodyToMono(localVarReturnType);
    }

    /**
     * Check username availability
     * Checks if a username is already taken
     * <p><b>200</b> - Successfully checked username
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Boolean>> usernameExistsWithHttpInfo(String username) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return usernameExistsRequestCreation(username).toEntity(localVarReturnType);
    }

    /**
     * Check username availability
     * Checks if a username is already taken
     * <p><b>200</b> - Successfully checked username
     * <p><b>500</b> - Internal server error
     * @param username The username parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec usernameExistsWithResponseSpec(String username) throws WebClientResponseException {
        return usernameExistsRequestCreation(username);
    }
}
