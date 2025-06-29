package com.example.dorm.service;

import com.example.dorm.model.Contract;
import com.example.dorm.model.Room;
import com.example.dorm.model.Student;
import com.example.dorm.repository.ContractRepository;
import com.example.dorm.repository.StudentRepository;
import com.example.dorm.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractServiceTest {
    @Mock ContractRepository contractRepository;
    @Mock StudentRepository studentRepository;
    @Mock RoomRepository roomRepository;
    @InjectMocks ContractService contractService;

    @Test
    void testGetAllContractsPageable() {
        Page<Contract> page = new PageImpl<>(List.of(new Contract()));
        when(contractRepository.findAll(any(Pageable.class))).thenReturn(page);
        assertEquals(1, contractService.getAllContracts(Pageable.unpaged()).getTotalElements());
    }
    @Test
    void testGetAllContractsList() {
        when(contractRepository.findAll()).thenReturn(List.of(new Contract()));
        assertEquals(1, contractService.getAllContracts().size());
    }
    @Test
    void testGetContractFound() {
        Contract c = new Contract(); c.setId(1L);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(c));
        assertTrue(contractService.getContract(1L).isPresent());
    }
    @Test
    void testGetContractNotFound() {
        when(contractRepository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(contractService.getContract(2L).isPresent());
    }
    @Test
    void testCreateContractRoomFull() {
        Room room = new Room(); room.setId(1L); room.setCapacity(1);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(studentRepository.countByRoom_Id(1L)).thenReturn(1L);
        Contract contract = new Contract(); contract.setRoom(room);
        assertThrows(IllegalStateException.class, () -> contractService.createContract(contract));
    }
    @Test
    void testCreateContractStudentNotFound() {
        Room room = new Room(); room.setId(1L); room.setCapacity(2);
        Student student = new Student(); student.setId(2L);
        Contract contract = new Contract(); contract.setRoom(room); contract.setStudent(student);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(studentRepository.countByRoom_Id(1L)).thenReturn(0L);
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> contractService.createContract(contract));
    }

    @Test
    void testCreateContractStudentAlreadyHasContract() {
        Room room = new Room(); room.setId(1L); room.setCapacity(2);
        Student student = new Student(); student.setId(2L);
        Contract contract = new Contract(); contract.setRoom(room); contract.setStudent(student);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(studentRepository.countByRoom_Id(1L)).thenReturn(0L);
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(contractRepository.existsByStudent_Id(2L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> contractService.createContract(contract));
    }
    @Test
    void testCreateContractSuccess() {
        Room room = new Room(); room.setId(1L); room.setCapacity(2);
        Student student = new Student(); student.setId(2L);
        Contract contract = new Contract(); contract.setRoom(room); contract.setStudent(student);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(studentRepository.countByRoom_Id(1L)).thenReturn(0L);
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(contractRepository.save(any())).thenReturn(contract);
        when(studentRepository.save(any())).thenReturn(student);
        Contract result = contractService.createContract(contract);
        assertNotNull(result);
    }
    @Test
    void testUpdateContractNotFound() {
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> contractService.updateContract(1L, new Contract()));
    }
    @Test
    void testDeleteContract() {
        doNothing().when(contractRepository).deleteById(1L);
        contractService.deleteContract(1L);
        verify(contractRepository, times(1)).deleteById(1L);
    }
    @Test
    void testSearchContracts() {
        Page<Contract> page = new PageImpl<>(List.of(new Contract()));
        when(contractRepository.findAll(any(Pageable.class))).thenReturn(page);
        assertEquals(1, contractService.searchContracts(null, Pageable.unpaged()).getTotalElements());
    }
    @Test
    void testFindLatestContractByStudentId() {
        Contract c = new Contract();
        when(contractRepository.findTopByStudent_IdOrderByEndDateDesc(1L)).thenReturn(c);
        assertNotNull(contractService.findLatestContractByStudentId(1L));
    }
} 