from mitmproxy import http
import json

# Define the URLs and headers to modify
def response(flow: http.HTTPFlow) -> None:
    api_url = flow.request.pretty_url
    if api_url in urls_to_mock:
        # Modify the specified headers
        for header, value in urls_to_mock[api_url].items():
            flow.response.headers[header] = value

        # Save headers to a file
        headers = {k: v for k, v in flow.response.headers.items()}
        with open("modified_headers.json", "w") as file:
            json.dump(headers, file, indent=2)
        print(f"Modified headers for {api_url}: {headers}")
