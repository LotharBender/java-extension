package actions.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import cds.gen.managepostingdocumentservice.XCorrectionPostingContext;
import cds.gen.managepostingdocumentservice.ManagePostingDocumentService_;
import cds.gen.managepostingdocumentservice.PostingDocuments;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cds.services.ErrorStatus;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.messages.Message;
import com.sap.cds.services.messages.Messages;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import static com.sap.cds.Struct.access;

@RequestScope
@Component
@ServiceName(ManagePostingDocumentService_.CDS_NAME)
public class CorrectionPosting implements EventHandler {

    @Autowired
    Messages messages;
    @On(service = ManagePostingDocumentService_.CDS_NAME)
    public void createCorrectionPosting(final XCorrectionPostingContext context) {

        String mockUrl = "https://correctionposting.free.beeceptor.com/my/api/CorrectionPosting";
        HttpPost httpPost = new HttpPost(mockUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        ErrorStatus responseCode = null;

        PostingDocuments postingDocument = context.getPostingDocuments();
        String x_NewField1 = context.getXNewField1();
        String comments = context.getXComments();
        String businessPartner = context.getBusinessPartner();

        //Business validation and Transformation
        String payload = context.get("data").toString(); //Prepare the payload according to the target API
        //Make an outbound call
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            StringEntity httpPayload = new StringEntity(payload, StandardCharsets.UTF_8);
            httpPost.setEntity(httpPayload);

            try {
                CloseableHttpResponse response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String resp = EntityUtils.toString(entity);
                if (response.getStatusLine().getStatusCode() == 200) {
                    Map<String, Object> map = new ObjectMapper().readValue(resp, Map.class);
                    //Response Transformation and set the relevant data into the extended fields using the Typesafe Setters
                    postingDocument.setXNewField1(map.get("x_new_field1").toString());
                    postingDocument.setXNewField2(Integer.valueOf(map.get("x_new_field2").toString()));
                    //messages.success("Success");
                    context.setResult(postingDocument);

                } else if (response.getStatusLine().getStatusCode() == 400) {
                    messages.error("Test Error");
                    messages.warn ("Test Warning");
                    messages.info ("Test Info");
                } else {
                    responseCode = ErrorStatuses.getByCode(response.getStatusLine().getStatusCode());
                    throw new ServiceException(responseCode, "Outbound communication failed");
                }

            } catch (Exception e) {
                throw new ServiceException(responseCode, "Outbound communication failed");
            }

        } catch (IOException e) {
            throw new ServiceException(ErrorStatuses.SERVER_ERROR, "Outbound communication failed");
        }

		/*postingDocument.setXNewField1("TEST Correction posting");
		postingDocument.setXNewField2(1001);*/
        //messages.success("Success");
        //context.setResult(postingDocument);

        context.setCompleted();

    }

}

