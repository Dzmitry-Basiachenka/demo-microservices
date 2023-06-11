package contracts.song

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method DELETE()
        url("/songs") {
            queryParameters {
                parameter("ids", "1")
            }
        }
        headers {
            contentType('application/json')
        }
    }
    response {
        status OK()
        body(ids: [1])
        headers {
            contentType('application/json')
        }
    }
}
