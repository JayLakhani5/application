# HTTP Status Codes

This document provides a brief description of common HTTP status codes and their appropriate usage context.

| **Status Code** | **Name**                  | **Description**                                                                 | **Usage Context**                                                                                  |
|-----------------|---------------------------|---------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| **200**         | OK                        | The request was successful.                                                     | Used when a request is successfully processed by the server, typically for `GET`, `POST`, or `PUT`.  |
| **201**         | Created                   | The request was successful, and a new resource was created.                     | Used for `POST` requests when a resource is created (e.g., adding a new item in a database).         |
| **204**         | No Content                | The request was successful, but no content is returned.                         | Used for `DELETE` or `PUT` requests where no additional information needs to be sent in the response.|
| **301**         | Moved Permanently          | The resource has been permanently moved to a new URL.                           | Used in URL redirection, SEO, or when restructuring websites (permanent redirects).                  |
| **302**         | Found (Temporary Redirect) | The resource is temporarily available at a different URL.                       | Used for temporary URL redirections, informing the client to fetch a resource from another location.  |
| **400**         | Bad Request               | The server could not understand the request due to invalid syntax.              | Used when the client sends malformed data, such as invalid query parameters or body content.         |
| **401**         | Unauthorized              | Authentication is required and has failed or not been provided.                 | Used when access requires authentication (e.g., missing or invalid token in an API request).         |
| **403**         | Forbidden                 | The client does not have permission to access the resource.                     | Used when the user is authenticated but does not have the necessary permissions to access a resource.|
| **404**         | Not Found                 | The server cannot find the requested resource.                                  | Used when a resource (like a web page or API endpoint) is not available.                            |
| **405**         | Method Not Allowed        | The requested method is not supported for the resource.                         | Used when the client attempts to use an HTTP method (e.g., `POST`) that is not allowed for that resource.|
| **409**         | Conflict                  | The request conflicts with the current state of the resource.                   | Used in scenarios like duplicate entries or version conflicts in APIs (e.g., when updating data).    |
| **500**         | Internal Server Error      | The server encountered a general error that prevented it from fulfilling the request. | Used when an unexpected condition is encountered, often related to server-side bugs or misconfigurations. |
| **503**         | Service Unavailable        | The server is not ready to handle the request (e.g., due to maintenance or overload). | Used when the server is temporarily unable to handle requests, typically for scheduled downtime or high traffic. |

## How to Use

These status codes should be used in the response of HTTP requests to indicate the success, failure, or result of a request. Proper usage helps in debugging, improving user experience, and ensuring that the client (or end-user) understands the outcome of their request.
