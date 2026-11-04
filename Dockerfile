FROM golang:1.27-alpine AS builder

WORKDIR /go/src/app

RUN apk add --no-cache make git dpkg
COPY . .
RUN make build

FROM scratch
WORKDIR /
COPY --from=builder /go/src/app/kbot .
COPY --from=alpine:latest /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
ENTRYPOINT ["./kbot"]
