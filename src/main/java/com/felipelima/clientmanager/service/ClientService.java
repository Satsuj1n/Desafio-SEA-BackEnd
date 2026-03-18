package com.felipelima.clientmanager.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.felipelima.clientmanager.dto.request.ClientRequest;
import com.felipelima.clientmanager.dto.request.EmailRequest;
import com.felipelima.clientmanager.dto.request.PhoneRequest;
import com.felipelima.clientmanager.dto.response.AddressResponse;
import com.felipelima.clientmanager.dto.response.ClientResponse;
import com.felipelima.clientmanager.dto.response.EmailResponse;
import com.felipelima.clientmanager.dto.response.PhoneResponse;
import com.felipelima.clientmanager.entity.Address;
import com.felipelima.clientmanager.entity.Client;
import com.felipelima.clientmanager.entity.Email;
import com.felipelima.clientmanager.entity.Phone;
import com.felipelima.clientmanager.entity.enums.PhoneTypeEnum;
import com.felipelima.clientmanager.exception.BusinessException;
import com.felipelima.clientmanager.exception.ResourceNotFoundException;
import com.felipelima.clientmanager.repository.ClientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

        private final ClientRepository clientRepository;

        /**
         * Creates a new client.
         *
         * Flow:
         * 1. Remove masks from CPF and phone numbers (store raw digits)
         * 2. Check if CPF already exists in the database
         * 3. Convert the request DTO to entity
         * 4. Save to database
         * 5. Convert entity back to response DTO (with masks)
         */
        @Transactional
        public ClientResponse create(ClientRequest request) {
                String rawCpf = MaskUtils.removeNonDigits(request.getCpf());

                if (clientRepository.existsByCpf(rawCpf)) {
                        throw new BusinessException("CPF already registered: " + request.getCpf());
                }

                Client client = toEntity(request);
                client.setCpf(rawCpf);

                Client saved = clientRepository.save(client);
                log.info("Client created: id={}, name={}", saved.getId(), saved.getName());

                return toResponse(saved);
        }

        /**
         * Returns all clients with masked data.
         */
        @Transactional(readOnly = true)
        public List<ClientResponse> findAll() {
                return clientRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Finds a client by ID.
         * Throws ResourceNotFoundException if not found (returns HTTP 404).
         */
        @SuppressWarnings("null")
        @Transactional(readOnly = true)
        public ClientResponse findById(Long id) {
                Client client = clientRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

                return toResponse(client);
        }

        /**
         * Updates an existing client.
         *
         * Important: checks if the new CPF conflicts with ANOTHER client
         * (not the one being updated — that's what existsByCpfAndIdNot does).
         */
        @SuppressWarnings("null")
        @Transactional
        public ClientResponse update(Long id, ClientRequest request) {
                Client client = clientRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

                String rawCpf = MaskUtils.removeNonDigits(request.getCpf());

                if (clientRepository.existsByCpfAndIdNot(rawCpf, id)) {
                        throw new BusinessException("CPF already registered by another client: " + request.getCpf());
                }

                updateEntity(client, request);
                client.setCpf(rawCpf);

                Client saved = clientRepository.save(client);
                log.info("Client updated: id={}, name={}", saved.getId(), saved.getName());

                return toResponse(saved);
        }

        /**
         * Deletes a client by ID.
         * Cascade removes address, phones, and emails automatically.
         */
        @SuppressWarnings("null")
        @Transactional
        public void delete(Long id) {
                Client client = clientRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

                clientRepository.delete(client);
                log.info("Client deleted: id={}, name={}", id, client.getName());
        }

        // ==========================================
        // Conversion methods: DTO <-> Entity
        // ==========================================

        /**
         * Converts ClientRequest DTO to Client entity.
         * This is where mask removal happens for phone numbers.
         * In DRF terms: equivalent to serializer.create() / serializer.save()
         */
        private Client toEntity(ClientRequest request) {
                Client client = new Client();
                client.setName(request.getName());

                Address address = new Address();
                address.setZipCode(MaskUtils.removeNonDigits(request.getAddress().getZipCode()));
                address.setStreet(request.getAddress().getStreet());
                address.setNeighborhood(request.getAddress().getNeighborhood());
                address.setCity(request.getAddress().getCity());
                address.setState(request.getAddress().getState());
                address.setComplement(request.getAddress().getComplement());
                client.setAddress(address);

                List<Phone> phones = request.getPhones().stream()
                                .map(this::toPhoneEntity)
                                .collect(Collectors.toList());
                client.setPhones(phones);

                List<Email> emails = request.getEmails().stream()
                                .map(this::toEmailEntity)
                                .collect(Collectors.toList());
                client.setEmails(emails);

                return client;
        }

        private Phone toPhoneEntity(PhoneRequest request) {
                Phone phone = new Phone();
                phone.setType(PhoneTypeEnum.valueOf(request.getType().toUpperCase()));
                phone.setNumber(MaskUtils.removeNonDigits(request.getNumber()));
                return phone;
        }

        private Email toEmailEntity(EmailRequest request) {
                Email email = new Email();
                email.setAddress(request.getAddress());
                return email;
        }

        /**
         * Updates an existing entity with new data from the request DTO.
         * For collections (phones, emails), we clear and re-add.
         * orphanRemoval=true in the entity ensures old items are deleted from DB.
         */
        private void updateEntity(Client client, ClientRequest request) {
                client.setName(request.getName());

                Address address = client.getAddress();
                address.setZipCode(MaskUtils.removeNonDigits(request.getAddress().getZipCode()));
                address.setStreet(request.getAddress().getStreet());
                address.setNeighborhood(request.getAddress().getNeighborhood());
                address.setCity(request.getAddress().getCity());
                address.setState(request.getAddress().getState());
                address.setComplement(request.getAddress().getComplement());

                client.getPhones().clear();
                client.getPhones().addAll(
                                request.getPhones().stream()
                                                .map(this::toPhoneEntity)
                                                .collect(Collectors.toList()));

                client.getEmails().clear();
                client.getEmails().addAll(
                                request.getEmails().stream()
                                                .map(this::toEmailEntity)
                                                .collect(Collectors.toList()));
        }

        /**
         * Converts Client entity to ClientResponse DTO.
         * This is where masks are APPLIED (CPF, zip code, phone).
         * In DRF terms: equivalent to serializer.to_representation()
         */
        private ClientResponse toResponse(Client client) {
                AddressResponse addressResponse = new AddressResponse(
                                MaskUtils.maskZipCode(client.getAddress().getZipCode()),
                                client.getAddress().getStreet(),
                                client.getAddress().getNeighborhood(),
                                client.getAddress().getCity(),
                                client.getAddress().getState(),
                                client.getAddress().getComplement());

                List<PhoneResponse> phoneResponses = client.getPhones().stream()
                                .map(phone -> new PhoneResponse(
                                                phone.getId(),
                                                phone.getType().name(),
                                                MaskUtils.maskPhone(phone.getNumber())))
                                .collect(Collectors.toList());

                List<EmailResponse> emailResponses = client.getEmails().stream()
                                .map(email -> new EmailResponse(
                                                email.getId(),
                                                email.getAddress()))
                                .collect(Collectors.toList());

                return new ClientResponse(
                                client.getId(),
                                client.getName(),
                                MaskUtils.maskCpf(client.getCpf()),
                                addressResponse,
                                phoneResponses,
                                emailResponses);
        }
}
