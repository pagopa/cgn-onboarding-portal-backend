/**
 * Insert fake data into CosmosDB database emulator.
 */
 import { BlobService } from "azure-storage";
 import { fromNullable } from "fp-ts/lib/Option";
 
 const blobConnectionString = fromNullable(
   process.env.BLOB_CONNECTION_STRING
 ).getOrElseL(() => {
   // tslint:disable-next-line: no-console
   console.error("Missing BLOB_CONNECTION_STRING property");
   process.exit();
 });
 
 const blobService = new BlobService(blobConnectionString);
 const createContainerTask = (containerName: string) =>
   blobService.createContainerIfNotExists(containerName, (err, _) => {
     if (err) {
       // tslint:disable-next-line: no-console
       console.error(err);
     }
     // tslint:disable-next-line: no-console
     console.info("Blob container created: ", containerName);
   });
 
 createContainerTask(
   fromNullable(process.env.IMAGES_CONTAINER_NAME).getOrElse("profileimages")
 );
 createContainerTask(
   fromNullable(process.env.DOCUMENTS_CONTAINER_NAME).getOrElse("userdocuments")
 );
 