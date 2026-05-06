# 🏆 FreeLink Platform: Presentation Guide (Live Demo)

## 🌟 Introduction
"Welcome to the FreeLink project. We have built a highly scalable, microservices-based freelance matching platform, fully automated with a professional CI/CD pipeline and deployed on a self-managed Kubernetes cluster on Azure."

---

## 🛠️ PHASE 1: The DevOps Engine (Jenkins)
**Objective**: Show that you are a pro at automation.

1.  **Open Jenkins**: `http://localhost:8080` (or your local Jenkins IP)
2.  **Show the `MASTER-PIPELINE`**:
    *   **Click Build**: Explain that this single button orchestrates the compilation, testing, and deployment of **13 microservices**.
    *   **Explain the Workflow**: "We use Maven for builds, SonarQube for code quality, Docker for containerization, and the image is pushed to Docker Hub."
3.  **Show the `Global-CD`**:
    *   Explain: "Once all images are ready, this pipeline connects to our Azure cluster via SSH and updates all Kubernetes pods automatically."

---

## 🔐 PHASE 2: Identity & Security (Keycloak)
**Objective**: Show how you handle professional security.

*   **URL**: `http://20.240.47.244:30090`
*   **Username**: `admin` / **Password**: `admin`
*   **What to show**:
    *   Show the **`freelink`** realm.
    *   Show the **`test1@exemple.com`** user.
    *   Explain: "We use Keycloak for OAuth2 and OpenID Connect. This ensures that our user data is secure and separated from our business logic."

---

## 🌐 PHASE 3: The Application (Frontend)
**Objective**: Show the final product.

*   **URL**: `http://20.240.47.244:30080`
*   **The Demo Flow**:
    1.  **Landing Page**: Show the professional UI.
    2.  **Login**: Use the user you created in Keycloak.
    3.  **Features**: Browse freelancers, check skills, or view portfolios.
    4.  **Explain the Gateway**: "All requests pass through our **Spring Cloud Gateway** (Port 30082) which handles routing and security checks."

---

## 📈 PHASE 4: Monitoring & Observability
**Objective**: The "Bonus" that gets you the highest grade.

*   **Prometheus**: `http://20.240.47.244:30001`
    *   *Show*: Status -> Targets. Explain: "We are collecting real-time health data from all 13 microservices."
*   **Grafana**: `http://20.240.47.244:30002`
    *   *Login*: `admin` / `admin`
    *   *Explain*: "This is our control center. We can see CPU usage, memory consumption, and request counts for the entire platform."

---

## 🏁 Final Conclusion
"In conclusion, FreeLink is not just a website. It is a modern, cloud-native infrastructure that leverages:
*   **Microservices** (Spring Boot)
*   **Orchestration** (Kubernetes on Azure)
*   **Security** (Keycloak)
*   **Observability** (Prometheus/Grafana)
*   **Automation** (Jenkins Master Pipeline)"

**Thank you for your attention. Any questions?** 🚀🏆🏁
