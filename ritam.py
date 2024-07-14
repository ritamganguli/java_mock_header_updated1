from mitmproxy import http
import json

# Define the URL and headers to modify
api_url = 'https://www.lambdatest.com/resources/js/zohocrm.js'
headers_to_mock = {

}

def response(flow: http.HTTPFlow) -> None:
    if flow.request.pretty_url == api_url:
        # Modify the specified headers
        for header, value in headers_to_mock.items():
            flow.response.headers[header] = value

        # Save headers to a file
        headers = {k: v for k, v in flow.response.headers.items()}
        with open("modified_headers.json", "w") as file:
            json.dump(headers, file, indent=2)
