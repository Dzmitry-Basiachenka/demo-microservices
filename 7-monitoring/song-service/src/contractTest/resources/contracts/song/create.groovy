package contracts.song

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method POST()
        url "/songs"
        body(
                id: 1,
                name: "Song",
                artist: "John Doe",
                album: "Songs",
                length: "60",
                released: "2020"
        )
        headers {
            contentType('application/json')
        }
    }
    response {
        status OK()
        body(id: 1)
        headers {
            contentType('application/json')
        }
    }
}
