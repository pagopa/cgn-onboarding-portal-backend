package it.gov.pagopa.cgn.portal.eycaintegration;


import org.springframework.stereotype.Component;

@Component
public class EycaIntegrationClient {

   /* public void addPet(DiscountRequestEycaIntegration body,  String accessToken,) throws ApiException {
        if (accessToken == null) {
            throw new ApiException(
                    400, "Missing the required parameter 'accessToken' when calling devicelist");
        }

        Object postBody = body;

        // create path and map variables
        String path = "/pet".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> queryParams = new ArrayList<Pair>();
        Map<String, String> headerParams = new HashMap<String, String>();
        Map<String, Object> formParams = new HashMap<String, Object>();

        final String[] accepts = {"application/json", "application/xml"};
        final String accept = apiClient.selectHeaderAccept(accepts);

        final String[] contentTypes = {"application/json", "application/xml"};
        final String contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {"petstore_auth"};

        apiClient.invokeAPI(
                path,
                "POST",
                queryParams,
                postBody,
                headerParams,
                formParams,
                accept,
                contentType,
                authNames,
                null);
    }
*/
}
