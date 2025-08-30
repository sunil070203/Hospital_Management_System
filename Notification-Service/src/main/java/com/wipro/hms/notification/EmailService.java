@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification sendSimpleEmail(String to, String subject, String message) { ... }
    public Notification sendHtmlEmail(String to, String subject, String message) { ... }
}
