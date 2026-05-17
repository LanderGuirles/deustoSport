package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.UsuarioRepository;
import com.deustosport.my_app.repository.CredencialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para eliminación de cuentas.
 * Prueba el flujo completo de eliminación con base de datos real.
 */
@SpringBootTest
@Transactional
class EliminacionCuentaIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioTest;
    private Long usuarioTestId;
    private String contrasenaOriginal;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        contrasenaOriginal = "TestPassword123!";
        usuarioTest = new Usuario();
        usuarioTest.setNombreCompleto("Test Usuario Eliminacion");
        usuarioTest.setEmail("test-eliminacion@example.com");
        usuarioTest.setDni("99999999Z");
        usuarioTest.setRol(Rol.CLIENTE);
        usuarioTest.setActivo(true);
        usuarioTest.setEsSocio(false);
        usuarioTest.setBilletera(BigDecimal.ZERO);
        usuarioTest.setFechaNacimiento(LocalDate.of(1995, 3, 10));
        usuarioTest.setTelefono("999999999");

        usuarioTest = usuarioRepository.save(usuarioTest);
        usuarioTestId = usuarioTest.getId();

        // Crear credencial
        var credencial = new com.deustosport.my_app.entity.Credencial();
        credencial.setUsuario(usuarioTest);
        credencial.setPasswordHash(passwordEncoder.encode(contrasenaOriginal));
        credencial.setActivo(true);
        credencial.setFechaCreacion(java.time.LocalDateTime.now());
        credencialRepository.save(credencial);
    }

    /**
     * Test: Validar que puede ser eliminado sin problemas
     */
    @Test
    void testPuedeSerEliminado_SinProblemas() {
        // Given
        assertTrue(usuarioRepository.existsById(usuarioTestId));
        assertEquals(BigDecimal.ZERO, usuarioTest.getBilletera());

        // When
        boolean resultado = usuarioService.puedeSerEliminado(usuarioTestId);

        // Then
        assertTrue(resultado);
    }

    /**
     * Test: No puede ser eliminado si tiene saldo en billetera
     */
    @Test
    void testPuedeSerEliminado_ConSaldo_NoPuede() {
        // Given
        usuarioTest.setBilletera(new BigDecimal("50.00"));
        usuarioRepository.save(usuarioTest);

        // When
        boolean resultado = usuarioService.puedeSerEliminado(usuarioTestId);

        // Then
        assertFalse(resultado);
    }

    /**
     * Test: Obtener motivos por los que no puede eliminarse
     */
    @Test
    void testObtenerMotivoNoEliminable_ConSaldo() {
        // Given
        usuarioTest.setBilletera(new BigDecimal("75.50"));
        usuarioRepository.save(usuarioTest);

        // When
        String motivos = usuarioService.obtenerMotivoNoEliminable(usuarioTestId);

        // Then
        assertNotNull(motivos);
        assertTrue(motivos.contains("billetera"));
        assertTrue(motivos.contains("75.50"));
    }

    /**
     * Test: Eliminar cuenta exitosamente
     */
    @Test
    void testSolicitarEliminacionCuenta_Exitoso() {
        // Given
        assertTrue(usuarioRepository.existsById(usuarioTestId));
        String motivoEliminacion = "NO_SATISFECHOS";

        // When
        boolean resultado = usuarioService.solicitarEliminacionCuenta(
                usuarioTestId,
                contrasenaOriginal,
                motivoEliminacion
        );

        // Then
        assertTrue(resultado);
        assertFalse(usuarioRepository.existsById(usuarioTestId));
        assertTrue(credencialRepository.findByUsuarioId(usuarioTestId).isEmpty());
    }

    /**
     * Test: No puede eliminarse con contraseña incorrecta
     */
    @Test
    void testSolicitarEliminacionCuenta_ContraseniaIncorrecta() {
        // Given
        String contrasenaIncorrecta = "PasswordIncorrecto123!";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.solicitarEliminacionCuenta(
                    usuarioTestId,
                    contrasenaIncorrecta,
                    "NO_SATISFECHOS"
            );
        });

        // Verificar que el usuario no fue eliminado
        assertTrue(usuarioRepository.existsById(usuarioTestId));
    }

    /**
     * Test: No puede eliminarse un usuario que no existe
     */
    @Test
    void testSolicitarEliminacionCuenta_UsuarioNoExiste() {
        // Given
        Long usuarioNoExistente = 99999L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.solicitarEliminacionCuenta(
                    usuarioNoExistente,
                    contrasenaOriginal,
                    "NO_SATISFECHOS"
            );
        });
    }

    /**
     * Test: Eliminar con diferentes motivos válidos
     */
    @Test
    void testSolicitarEliminacionCuenta_DiferentesMotivos() {
        // Given
        String[] motivos = {"PRIVACIDAD", "NO_UTILIZAMOS", "MIGRACION", "OTRO"};

        for (String motivo : motivos) {
            // Setup
            usuarioTest = new Usuario();
            usuarioTest.setNombreCompleto("Usuario Test " + motivo);
            usuarioTest.setEmail("test-" + motivo + "@example.com");
            usuarioTest.setDni("DNI" + motivo);
            usuarioTest.setRol(Rol.CLIENTE);
            usuarioTest.setActivo(true);
            usuarioTest.setEsSocio(false);
            usuarioTest.setBilletera(BigDecimal.ZERO);
            usuarioTest = usuarioRepository.save(usuarioTest);

            var credencial = new com.deustosport.my_app.entity.Credencial();
            credencial.setUsuario(usuarioTest);
            credencial.setPasswordHash(passwordEncoder.encode(contrasenaOriginal));
            credencial.setActivo(true);
            credencial.setFechaCreacion(java.time.LocalDateTime.now());
            credencialRepository.save(credencial);

            // When
            boolean resultado = usuarioService.solicitarEliminacionCuenta(
                    usuarioTest.getId(),
                    contrasenaOriginal,
                    motivo
            );

            // Then
            assertTrue(resultado, "No se pudo eliminar con motivo: " + motivo);
            assertFalse(usuarioRepository.existsById(usuarioTest.getId()));
        }
    }

    /**
     * Test: Después de eliminar, no se puede acceder al usuario
     */
    @Test
    void testDespuesDeEliminar_UsuarioNoAccesible() {
        // Given
        Long idAEliminar = usuarioTestId;

        // When
        usuarioService.solicitarEliminacionCuenta(idAEliminar, contrasenaOriginal, "NO_SATISFECHOS");

        // Then
        var usuarioEliminado = usuarioRepository.findById(idAEliminar);
        assertFalse(usuarioEliminado.isPresent());

        var credencialEliminada = credencialRepository.findByUsuarioId(idAEliminar);
        assertFalse(credencialEliminada.isPresent());
    }

    /**
     * Test: Datos de usuario se eliminan completamente
     */
    @Test
    void testEliminacionCompleta_TodosLosDatos() {
        // Given
        Long idAEliminar = usuarioTestId;
        String emailOriginal = usuarioTest.getEmail();

        // When
        usuarioService.solicitarEliminacionCuenta(idAEliminar, contrasenaOriginal, "PRIVACIDAD");

        // Then
        // Usuario no existe
        assertFalse(usuarioRepository.findById(idAEliminar).isPresent());

        // Email no se puede encontrar
        assertFalse(usuarioRepository.findByEmail(emailOriginal).isPresent());

        // Credenciales no existen
        assertFalse(credencialRepository.findByUsuarioId(idAEliminar).isPresent());
    }

    /**
     * Test: Validar búsqueda de usuario no elimina datos intermedios
     */
    @Test
    void testValidacion_NoAfectaOtrosUsuarios() {
        // Given
        var otroUsuario = new Usuario();
        otroUsuario.setNombreCompleto("Otro Usuario");
        otroUsuario.setEmail("otro@example.com");
        otroUsuario.setDni("88888888Y");
        otroUsuario.setRol(Rol.CLIENTE);
        otroUsuario.setActivo(true);
        otroUsuario.setBilletera(BigDecimal.ZERO);
        otroUsuario = usuarioRepository.save(otroUsuario);

        // When
        boolean puedeEliminar = usuarioService.puedeSerEliminado(usuarioTestId);

        // Then
        assertTrue(puedeEliminar);
        assertTrue(usuarioRepository.existsById(otroUsuario.getId()));
    }
}
