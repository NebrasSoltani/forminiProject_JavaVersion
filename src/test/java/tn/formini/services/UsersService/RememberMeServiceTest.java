package tn.formini.services.UsersService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RememberMeService
 */
public class RememberMeServiceTest {
    
    private RememberMeService rememberMeService;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "testPassword123";
    
    @BeforeEach
    public void setUp() {
        rememberMeService = new RememberMeService();
        // Clear any existing test data
        rememberMeService.clearCredentials();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up after each test
        rememberMeService.clearCredentials();
    }
    
    @Test
    public void testSaveAndRetrieveCredentials() {
        // Save credentials
        rememberMeService.saveCredentials(TEST_EMAIL, TEST_PASSWORD);
        
        // Verify they were saved
        assertTrue(rememberMeService.hasSavedCredentials());
        assertTrue(rememberMeService.isRememberMeChecked());
        assertEquals(TEST_EMAIL, rememberMeService.getSavedEmail());
        assertEquals(TEST_PASSWORD, rememberMeService.getSavedPassword());
    }
    
    @Test
    public void testClearCredentials() {
        // Save credentials first
        rememberMeService.saveCredentials(TEST_EMAIL, TEST_PASSWORD);
        assertTrue(rememberMeService.hasSavedCredentials());
        
        // Clear them
        rememberMeService.clearCredentials();
        
        // Verify they're cleared
        assertFalse(rememberMeService.hasSavedCredentials());
        assertFalse(rememberMeService.isRememberMeChecked());
        assertNull(rememberMeService.getSavedEmail());
        assertNull(rememberMeService.getSavedPassword());
    }
    
    @Test
    public void testSetRememberMeFlag() {
        // Test setting flag without credentials
        rememberMeService.setRememberMeFlag(true);
        assertTrue(rememberMeService.isRememberMeChecked());
        assertFalse(rememberMeService.hasSavedCredentials());
        
        // Test unsetting flag
        rememberMeService.setRememberMeFlag(false);
        assertFalse(rememberMeService.isRememberMeChecked());
    }
    
    @Test
    public void testEmptyCredentials() {
        // Test with empty/null credentials
        rememberMeService.saveCredentials("", TEST_PASSWORD);
        assertFalse(rememberMeService.hasSavedCredentials());
        
        rememberMeService.saveCredentials(TEST_EMAIL, "");
        assertFalse(rememberMeService.hasSavedCredentials());
        
        rememberMeService.saveCredentials(null, TEST_PASSWORD);
        assertFalse(rememberMeService.hasSavedCredentials());
        
        rememberMeService.saveCredentials(TEST_EMAIL, null);
        assertFalse(rememberMeService.hasSavedCredentials());
    }
    
    @Test
    public void testInitialState() {
        // Test initial state (no credentials saved)
        assertFalse(rememberMeService.hasSavedCredentials());
        assertFalse(rememberMeService.isRememberMeChecked());
        assertNull(rememberMeService.getSavedEmail());
        assertNull(rememberMeService.getSavedPassword());
    }
}
