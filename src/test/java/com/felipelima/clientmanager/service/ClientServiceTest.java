package com.felipelima.clientmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.felipelima.clientmanager.dto.request.AddressRequest;
import com.felipelima.clientmanager.dto.request.ClientRequest;
import com.felipelima.clientmanager.dto.request.EmailRequest;
import com.felipelima.clientmanager.dto.request.PhoneRequest;
import com.felipelima.clientmanager.dto.response.ClientResponse;
import com.felipelima.clientmanager.entity.Address;
import com.felipelima.clientmanager.entity.Client;
import com.felipelima.clientmanager.entity.Email;
import com.felipelima.clientmanager.entity.Phone;
import com.felipelima.clientmanager.entity.enums.PhoneTypeEnum;
import com.felipelima.clientmanager.exception.BusinessException;
import com.felipelima.clientmanager.exception.ResourceNotFoundException;
import com.felipelima.clientmanager.repository.ClientRepository;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientRequest clientRequest;
    private Client clientEntity;

    @BeforeEach
    void setUp() {
        AddressRequest addressRequest = new AddressRequest(
                "70040-010", "SBS Quadra 2", "Asa Sul", "Brasilia", "DF", null);

        PhoneRequest phoneRequest = new PhoneRequest("MOBILE", "(61) 99999-8888");
        EmailRequest emailRequest = new EmailRequest("joao@email.com");

        clientRequest = new ClientRequest(
                "Joao Silva",
                "123.456.789-09",
                addressRequest,
                Collections.singletonList(phoneRequest),
                Collections.singletonList(emailRequest));

        Address address = new Address(1L, "70040010", "SBS Quadra 2", "Asa Sul", "Brasilia", "DF", null);

        Phone phone = new Phone(1L, PhoneTypeEnum.MOBILE, "61999998888");
        Email email = new Email(1L, "joao@email.com");

        clientEntity = new Client();
        clientEntity.setId(1L);
        clientEntity.setName("Joao Silva");
        clientEntity.setCpf("12345678909");
        clientEntity.setAddress(address);
        clientEntity.setPhones(new ArrayList<>(Collections.singletonList(phone)));
        clientEntity.setEmails(new ArrayList<>(Collections.singletonList(email)));
    }

    // ==========================================
    // CREATE
    // ==========================================

    @Nested
    @DisplayName("Create client")
    class CreateTests {

        @Test
        @DisplayName("Should create client successfully")
        void createSuccess() {
            when(clientRepository.existsByCpf("12345678909")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);

            ClientResponse response = clientService.create(clientRequest);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Joao Silva", response.getName());
            assertEquals("123.456.789-09", response.getCpf());
            assertEquals("70040-010", response.getAddress().getZipCode());
            assertEquals("(61) 99999-8888", response.getPhones().get(0).getNumber());
            assertEquals("joao@email.com", response.getEmails().get(0).getAddress());

            verify(clientRepository).existsByCpf("12345678909");
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should store CPF without mask")
        void createStoresCpfWithoutMask() {
            when(clientRepository.existsByCpf("12345678909")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);

            clientService.create(clientRequest);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());

            assertEquals("12345678909", captor.getValue().getCpf());
        }

        @Test
        @DisplayName("Should throw BusinessException for duplicate CPF")
        void createDuplicateCpf() {
            when(clientRepository.existsByCpf("12345678909")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> clientService.create(clientRequest));

            assertTrue(exception.getMessage().contains("CPF already registered"));
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should store phone number without mask")
        void createStoresPhoneWithoutMask() {
            when(clientRepository.existsByCpf("12345678909")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);

            clientService.create(clientRequest);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());

            assertEquals("61999998888", captor.getValue().getPhones().get(0).getNumber());
        }

        @Test
        @DisplayName("Should store zip code without mask")
        void createStoresZipCodeWithoutMask() {
            when(clientRepository.existsByCpf("12345678909")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);

            clientService.create(clientRequest);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());

            assertEquals("70040010", captor.getValue().getAddress().getZipCode());
        }
    }

    // ==========================================
    // FIND ALL
    // ==========================================

    @Nested
    @DisplayName("Find all clients")
    class FindAllTests {

        @Test
        @DisplayName("Should return all clients with masks applied")
        void findAllSuccess() {
            when(clientRepository.findAll()).thenReturn(Collections.singletonList(clientEntity));

            List<ClientResponse> result = clientService.findAll();

            assertEquals(1, result.size());
            assertEquals("123.456.789-09", result.get(0).getCpf());
            assertEquals("70040-010", result.get(0).getAddress().getZipCode());
        }

        @Test
        @DisplayName("Should return empty list when no clients exist")
        void findAllEmpty() {
            when(clientRepository.findAll()).thenReturn(Collections.emptyList());

            List<ClientResponse> result = clientService.findAll();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return multiple clients")
        void findAllMultiple() {
            Client secondClient = new Client();
            secondClient.setId(2L);
            secondClient.setName("Maria Souza");
            secondClient.setCpf("98765432100");
            secondClient.setAddress(new Address(2L, "01001000", "Praca da Se", "Se", "Sao Paulo", "SP", null));
            secondClient.setPhones(new ArrayList<>(Collections.singletonList(
                    new Phone(2L, PhoneTypeEnum.RESIDENTIAL, "1133334444"))));
            secondClient.setEmails(new ArrayList<>(Collections.singletonList(
                    new Email(2L, "maria@email.com"))));

            when(clientRepository.findAll()).thenReturn(Arrays.asList(clientEntity, secondClient));

            List<ClientResponse> result = clientService.findAll();

            assertEquals(2, result.size());
        }
    }

    // ==========================================
    // FIND BY ID
    // ==========================================

    @Nested
    @DisplayName("Find client by ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should find client by ID with masks")
        void findByIdSuccess() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(clientEntity));

            ClientResponse response = clientService.findById(1L);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Joao Silva", response.getName());
            assertEquals("123.456.789-09", response.getCpf());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent ID")
        void findByIdNotFound() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> clientService.findById(999L));

            assertTrue(exception.getMessage().contains("999"));
        }
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @Nested
    @DisplayName("Update client")
    class UpdateTests {

        @Test
        @DisplayName("Should update client successfully")
        void updateSuccess() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(clientEntity));
            when(clientRepository.existsByCpfAndIdNot("12345678909", 1L)).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);

            ClientRequest updateRequest = new ClientRequest(
                    "Joao Silva Updated",
                    "123.456.789-09",
                    new AddressRequest("70040-010", "SBS Quadra 2 Bloco A", "Asa Sul", "Brasilia", "DF", "Sala 501"),
                    Collections.singletonList(new PhoneRequest("MOBILE", "(61) 99999-7777")),
                    Collections.singletonList(new EmailRequest("joao.novo@email.com")));

            ClientResponse response = clientService.update(1L, updateRequest);

            assertNotNull(response);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent client")
        void updateNotFound() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clientService.update(999L, clientRequest));

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when CPF belongs to another client")
        void updateDuplicateCpf() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(clientEntity));
            when(clientRepository.existsByCpfAndIdNot("12345678909", 1L)).thenReturn(true);

            assertThrows(BusinessException.class,
                    () -> clientService.update(1L, clientRequest));

            verify(clientRepository, never()).save(any());
        }
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Nested
    @DisplayName("Delete client")
    class DeleteTests {

        @Test
        @DisplayName("Should delete client successfully")
        void deleteSuccess() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(clientEntity));
            doNothing().when(clientRepository).delete(clientEntity);

            assertDoesNotThrow(() -> clientService.delete(1L));

            verify(clientRepository).delete(clientEntity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent client")
        void deleteNotFound() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clientService.delete(999L));

            verify(clientRepository, never()).delete(any());
        }
    }
}
