package com.example.dorm.controller;

import com.example.dorm.model.Contract;
import com.example.dorm.service.ContractService;
import com.example.dorm.service.StudentService;
import com.example.dorm.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/contracts")
public class ContractController {
    @Autowired
    private ContractService contractService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private RoomService roomService;

    @GetMapping
    public String listContracts(@RequestParam(value = "search", required = false) String search,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size,
                                Model model) {
        try {
            var pageable = org.springframework.data.domain.PageRequest.of(page, size);
            var contractsPage = contractService.searchContracts(search, pageable);
            model.addAttribute("contractsPage", contractsPage);
            int totalPages = contractsPage.getTotalPages();
            if (totalPages > 0) {
                java.util.List<Integer> pageNumbers =
                        java.util.stream.IntStream.rangeClosed(1, totalPages)
                                .boxed().toList();
                model.addAttribute("pageNumbers", pageNumbers);
            }
            model.addAttribute("search", search);
            return "contracts/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lấy danh sách hợp đồng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            model.addAttribute("contract", new Contract());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "contracts/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi hiển thị form tạo hợp đồng: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String createContract(@Valid @ModelAttribute Contract contract, BindingResult result, Model model,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "contracts/form";
        }
        if (contract.getStudent() == null || contract.getStudent().getId() == null) {
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("errorMessage", "Vui lòng chọn sinh viên hợp lệ");
            return "contracts/form";
        }
        try {
            contractService.createContract(contract);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo hợp đồng thành công");
            return "redirect:/contracts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lưu hợp đồng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{id}")
    public String viewContract(@PathVariable("id") Long id, Model model) {
        try {
            Optional<Contract> contractOptional = contractService.getContract(id);
            if (contractOptional.isPresent()) {
                model.addAttribute("contract", contractOptional.get());
                return "contracts/detail";
            } else {
                model.addAttribute("errorMessage", "Không tìm thấy hợp đồng với ID: " + id);
                return "error";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi xem hợp đồng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        try {
            Optional<Contract> contractOptional = contractService.getContract(id);
            if (contractOptional.isPresent()) {
                model.addAttribute("contract", contractOptional.get());
                model.addAttribute("rooms", roomService.getAllRooms());
                return "contracts/form";
            } else {
                model.addAttribute("errorMessage", "Không tìm thấy hợp đồng với ID: " + id);
                return "error";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải form chỉnh sửa: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}")
    public String updateContract(@PathVariable("id") Long id, @Valid @ModelAttribute Contract contract, BindingResult result, Model model,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "contracts/form";
        }
        if (contract.getStudent() == null || contract.getStudent().getId() == null) {
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("errorMessage", "Vui lòng chọn sinh viên hợp lệ");
            return "contracts/form";
        }
        try {
            contractService.updateContract(id, contract);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hợp đồng thành công");
            return "redirect:/contracts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật hợp đồng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteContract(@PathVariable("id") Long id, Model model,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            Optional<Contract> contractOptional = contractService.getContract(id);
            if (contractOptional.isPresent()) {
                contractService.deleteContract(id);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa hợp đồng thành công");
                return "redirect:/contracts";
            } else {
                model.addAttribute("errorMessage", "Không tìm thấy hợp đồng với ID: " + id);
                return "error";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi xoá hợp đồng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping(value = "/search", produces = "application/json")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> autocomplete(@RequestParam("term") String term) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var contractsPage = contractService.searchContractsAutocomplete(term, pageable);
        return contractsPage.getContent().stream().map(c -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getId());
            if (c.getStudent() != null) {
                map.put("label", c.getId() + " - " + c.getStudent().getCode() + " - " + c.getStudent().getName());
            } else {
                map.put("label", String.valueOf(c.getId()));
            }
            return map;
        }).toList();
    }

    // search handled by listContracts
}