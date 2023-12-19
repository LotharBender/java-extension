namespace x_postingDocument.ext;
 
using {
  ManagePostingDocumentService,
  com.sap.di.irp.postingDocument.PostingDocuments
} from '_base';
 
extend PostingDocuments with { // 2 new fields....
  x_new_field1 : String ;
  x_new_field2 : Integer;
}
 
 
extend entity ManagePostingDocumentService.PostingDocuments with @(
  restrict              : [{
    grant: [
      '*',
      'x_correctionPosting'
    ],
    to   : 'Posting'
  }],
  cds.redirection.target: true
) actions {                  
  @remote : {destination: 'remote-ext', deepSelect: true, timeOut: 1000}
  action x_correctionPosting(  @title: 'Manage Posting Document Ext Field1'  x_new_field1 : String,  @title: 'Business Partner'  business_partner : String,
  @title: 'Reason for Correction' x_comments : String  @UI.MultiLineText, postingDocuments : ManagePostingDocumentService.PostingDocuments @UI.Hidden   ) returns ManagePostingDocumentService.PostingDocuments;
 
};
 
 
 
// -------------------------------------------
// Fiori Annotations
 
annotate PostingDocuments : x_new_field1 with @title: 'Manage Posting Document Ext Field1';
annotate PostingDocuments : x_new_field2 with @title: 'Manage Posting Document Ext Field2';
 
annotate ManagePostingDocumentService.PostingDocuments with @UI.LineItem: {$value: [
  ...up to
  {Value: postingDocumentNumber},
  {Value: x_new_field1},
  {Value: x_new_field2},
  ...,
 
  {
    $Type : 'UI.DataFieldForAction',
    Label : 'Correction Posting',
    Action: 'ManagePostingDocumentService.x_correctionPosting'
  }
 
]};