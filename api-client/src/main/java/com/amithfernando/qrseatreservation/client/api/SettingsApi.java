package com.amithfernando.qrseatreservation.client.api;

import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

import com.amithfernando.qrseatreservation.client.model.ErrorResponse;
import com.amithfernando.qrseatreservation.client.model.Setting;
import com.amithfernando.qrseatreservation.client.model.UpdateSettingRequest;

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
public class SettingsApi {
    private ApiClient apiClient;

    public SettingsApi() {
        this(new ApiClient());
    }

    @Autowired
    public SettingsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get current settings
     * Retrieves the current application settings
     * <p><b>200</b> - Successfully retrieved settings
     * <p><b>500</b> - Internal server error
     * @return Setting
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSettingsRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<Setting> localVarReturnType = new ParameterizedTypeReference<Setting>() {};
        return apiClient.invokeAPI("/api/settings", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get current settings
     * Retrieves the current application settings
     * <p><b>200</b> - Successfully retrieved settings
     * <p><b>500</b> - Internal server error
     * @return Setting
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Setting> getSettings() throws WebClientResponseException {
        ParameterizedTypeReference<Setting> localVarReturnType = new ParameterizedTypeReference<Setting>() {};
        return getSettingsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get current settings
     * Retrieves the current application settings
     * <p><b>200</b> - Successfully retrieved settings
     * <p><b>500</b> - Internal server error
     * @return ResponseEntity&lt;Setting&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Setting>> getSettingsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Setting> localVarReturnType = new ParameterizedTypeReference<Setting>() {};
        return getSettingsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get current settings
     * Retrieves the current application settings
     * <p><b>200</b> - Successfully retrieved settings
     * <p><b>500</b> - Internal server error
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSettingsWithResponseSpec() throws WebClientResponseException {
        return getSettingsRequestCreation();
    }
    /**
     * Update settings
     * Updates the application settings. All fields are optional - only provided fields will be updated.
     * <p><b>200</b> - Successfully updated settings
     * <p><b>400</b> - Invalid settings data
     * <p><b>500</b> - Internal server error
     * @param updateSettingRequest The updateSettingRequest parameter
     * @return Setting
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateSettingsRequestCreation(UpdateSettingRequest updateSettingRequest) throws WebClientResponseException {
        Object postBody = updateSettingRequest;
        // verify the required parameter 'updateSettingRequest' is set
        if (updateSettingRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'updateSettingRequest' when calling updateSettings", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<Setting> localVarReturnType = new ParameterizedTypeReference<Setting>() {};
        return apiClient.invokeAPI("/api/settings", HttpMethod.PUT, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update settings
     * Updates the application settings. All fields are optional - only provided fields will be updated.
     * <p><b>200</b> - Successfully updated settings
     * <p><b>400</b> - Invalid settings data
     * <p><b>500</b> - Internal server error
     * @param updateSettingRequest The updateSettingRequest parameter
     * @return Setting
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Setting> updateSettings(UpdateSettingRequest updateSettingRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Setting> localVarReturnType = new ParameterizedTypeReference<Setting>() {};
        return updateSettingsRequestCreation(updateSettingRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Update settings
     * Updates the application settings. All fields are optional - only provided fields will be updated.
     * <p><b>200</b> - Successfully updated settings
     * <p><b>400</b> - Invalid settings data
     * <p><b>500</b> - Internal server error
     * @param updateSettingRequest The updateSettingRequest parameter
     * @return ResponseEntity&lt;Setting&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Setting>> updateSettingsWithHttpInfo(UpdateSettingRequest updateSettingRequest) throws WebClientResponseException {
        ParameterizedTypeReference<Setting> localVarReturnType = new ParameterizedTypeReference<Setting>() {};
        return updateSettingsRequestCreation(updateSettingRequest).toEntity(localVarReturnType);
    }

    /**
     * Update settings
     * Updates the application settings. All fields are optional - only provided fields will be updated.
     * <p><b>200</b> - Successfully updated settings
     * <p><b>400</b> - Invalid settings data
     * <p><b>500</b> - Internal server error
     * @param updateSettingRequest The updateSettingRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateSettingsWithResponseSpec(UpdateSettingRequest updateSettingRequest) throws WebClientResponseException {
        return updateSettingsRequestCreation(updateSettingRequest);
    }
}
