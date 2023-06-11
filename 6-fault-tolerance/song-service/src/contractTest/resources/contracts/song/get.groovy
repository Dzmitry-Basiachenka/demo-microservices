package contracts.song

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method GET()
        url "/songs/1"
    }
    response {
        status OK()
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
}
