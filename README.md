# Construction Manager - Professional Android Native App

A comprehensive, intuitive Android native application for home construction management built with modern Android development best practices. This app provides construction professionals with powerful tools to manage projects, track materials, monitor costs, and document progress throughout the entire construction lifecycle.

## 🏗️ Overview

Construction Manager is designed to mirror the natural workflow of home construction, from pre-construction planning to project handover. The app provides minimal-tap workflows with intelligent design patterns that construction industry professionals will find intuitive and efficient.

## ✨ Key Features

### 📊 **Comprehensive Dashboard**
- Real-time project overview with key metrics
- Active project count and budget tracking
- Phase distribution visualization
- Quick actions for common tasks
- Recent project timeline

### 🏢 **Project Lifecycle Management**
- **14-Phase Construction Workflow**: From pre-construction to handover
- **Project Status Tracking**: Planning, Active, On Hold, Completed, Cancelled
- **Budget Management**: Real-time cost tracking vs. budget with visual indicators
- **Timeline Management**: Start dates, estimated completion, progress tracking
- **Client Management**: Complete client information and communication history

### 🧱 **Comprehensive Material Catalog**
- **2025 Current Pricing**: Real-world construction material costs
- **25+ Material Categories**: Lumber, Concrete, Steel, Roofing, Electrical, Plumbing, HVAC, Windows, Doors, Hardware, Fixtures, and more
- **Regional Pricing Support**: Location-based pricing for accurate estimates
- **Supplier Integration**: Supplier information, SKUs, and contact details
- **Advanced Search & Filtering**: Find materials quickly by name, category, or supplier

### 👷 **Labor & Workforce Management**
- **Skilled Trade Categories**: 23 different trade types from general labor to specialized technicians
- **Skill Level Tracking**: Apprentice, Journeyman, Master, Specialist, Supervisor
- **Time Tracking**: Labor entries with regular and overtime calculations
- **Worker Profiles**: Contact info, certifications, emergency contacts
- **Cost Analysis**: Detailed labor cost breakdowns by phase and trade

### 🔄 **Construction Phase Workflows**
1. **Pre-Construction**: Permits, planning, site surveys, contractor scheduling
2. **Site Preparation**: Clearing, soil testing, utility marking, access roads
3. **Foundation**: Excavation, footings, waterproofing, backfill
4. **Framing**: Floor systems, wall framing, roof framing, sheathing
5. **Roofing**: Decking, underlayment, materials, flashing, gutters
6. **Siding & Exterior**: House wrap, siding, trim, windows, doors
7. **MEP Rough-In**: Electrical, plumbing, HVAC installation
8. **Insulation**: Thermal barriers, vapor barriers, air sealing
9. **Drywall**: Installation, taping, mudding, texturing
10. **Flooring**: Subfloor prep, hardwood, tile, carpet installation
11. **Interior Finishing**: Painting, trim, cabinets, countertops, fixtures
12. **Final Inspection**: Building inspections, punch lists, corrections
13. **Handover**: Walk-through, keys, warranties, documentation
14. **Completed**: Project closure and final documentation

### 📸 **Photo Documentation System**
- **Progress Photography**: Phase-by-phase visual documentation
- **Safety Compliance**: Safety equipment and protocol documentation
- **Quality Control**: Quality assurance checkpoints with photos
- **Material Verification**: Delivery and quality documentation
- **Issue Tracking**: Problem identification and resolution photos
- **Before/After Comparisons**: Transformation documentation

## 🏛️ Technical Architecture

### **Modern Android Stack (2025)**
- **Language**: Kotlin 1.9.10
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room with SQLite
- **Networking**: Retrofit + OkHttp
- **Async Operations**: Kotlin Coroutines + Flow
- **Image Loading**: Coil
- **Navigation**: Navigation Compose

### **Project Structure**
```
com.constructionmanager/
├── data/
│   ├── database/          # Room entities, DAOs, database setup
│   ├── repository/        # Data layer implementation
│   └── network/           # API services and network models
├── domain/
│   ├── model/            # Domain models (Project, Material, Labor)
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Business logic use cases
├── ui/
│   ├── components/       # Reusable UI components
│   ├── screens/          # Feature screens (Dashboard, Projects, Materials)
│   ├── theme/            # Material 3 theming
│   └── navigation/       # App navigation setup
└── di/                   # Dependency injection modules
```

### **Database Schema**
- **Projects**: Complete project lifecycle data
- **Materials**: Comprehensive material catalog with pricing
- **Labor Categories**: Trade types, skill levels, rates
- **Workers**: Workforce management and profiles
- **Labor Entries**: Time tracking and cost calculation
- **Photo Documentation**: Progress and quality photos

## 🎨 Design System

### **Construction Industry Theme**
- **Primary Color**: Construction Orange (#FF8C00) - Professional and recognizable
- **Secondary Color**: Steel Blue (#4682B4) - Industrial and trustworthy  
- **Accent Colors**: Concrete Gray, Safety Yellow - Industry-standard colors
- **Typography**: Clean, readable fonts optimized for outdoor/jobsite use
- **Material 3 Components**: Modern Android design language

### **User Experience Principles**
- **Minimal Taps**: Essential actions within 1-2 taps
- **Intuitive Navigation**: Mirror real construction workflows
- **Quick Actions**: Common tasks accessible from dashboard
- **Visual Feedback**: Clear progress indicators and status updates
- **Offline Support**: Core functionality works without internet

## 💰 2025 Construction Cost Data

The app includes comprehensive, current construction material pricing data:

### **Sample Material Pricing (2025)**
- **Dimensional Lumber 2x4x8**: $8.50 (varies by region: $7.85-$10.50)
- **Ready Mix Concrete 3000 PSI**: $185/cubic yard (regional: $175-$205)
- **Architectural Shingles**: $285/square (lifetime warranty)
- **Luxury Vinyl Plank**: $4.85/sq ft (premium grade)
- **Hardwood Oak 3/4"**: $8.95/sq ft (select grade)
- **PEX Tubing 1/2"x100ft**: $89.50/roll
- **Romex 12-2 Wire 250ft**: $165/roll (regional: $155-$185)

### **Regional Pricing Support**
- Northeast, Southeast, Midwest, Southwest, West Coast pricing variations
- Automatic location-based cost adjustments
- Local supplier integration capabilities

## 🚀 Getting Started

### **Prerequisites**
- Android Studio Iguana | 2023.2.1 or newer
- Android SDK 34 (API level 34)
- Minimum Android 8.0 (API level 26)
- Java 17 or newer

### **Setup Instructions**

1. **Clone the Repository**
   ```bash
   git clone https://github.com/tywade1980/ngbp-v2-0.git
   cd ngbp-v2-0
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository folder
   - Wait for Gradle sync to complete

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

4. **Run Tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

## 📱 App Screenshots & Features

### **Dashboard**
- Project overview with visual progress indicators
- Quick action buttons for common tasks
- Phase distribution charts
- Budget tracking with color-coded progress bars

### **Materials Catalog**
- Categorized browsing with visual category chips
- Advanced search with real-time filtering
- Regional pricing comparisons
- Supplier contact integration

### **Project Management**
- Status-based filtering (Active, Completed, Planning)
- Timeline visualization with milestone tracking
- Budget vs. actual cost analysis
- Phase-specific task management

### **Workflow Management**
- Phase-by-phase task breakdowns
- Estimated timeline calculations (117 working days typical)
- Progress tracking with visual indicators
- Task completion workflows

## 🔧 Configuration

### **Database Configuration**
The app automatically initializes with comprehensive seed data including:
- 25+ material categories with current 2025 pricing
- Regional pricing variations for major US regions
- Standard construction phases and workflows
- Sample project templates

### **Customization Options**
- Regional pricing preferences
- Default project templates
- Material supplier preferences
- Photo documentation categories

## 🤝 Contributing

We welcome contributions from construction industry professionals and Android developers!

### **Development Guidelines**
1. Follow Android Architecture Components best practices
2. Maintain Material 3 design consistency
3. Include comprehensive tests for new features
4. Update documentation for API changes
5. Ensure accessibility compliance

### **Feature Requests**
Current roadmap includes:
- AI-powered cost estimation
- Weather integration for scheduling
- Integration with major supplier APIs
- Advanced reporting and analytics
- Multi-project portfolio management

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Lead Developer**: Construction industry expertise meets modern Android development
- **Industry Consultants**: Active construction professionals ensuring real-world applicability
- **UX/UI Design**: Construction workflow specialists

## 🆘 Support

For technical support, feature requests, or industry-specific questions:
- **Issues**: Use GitHub Issues for bug reports and feature requests
- **Discussions**: Join our GitHub Discussions for general questions
- **Industry Feedback**: We value input from construction professionals

## 🏆 Recognition

This app represents the intersection of modern mobile technology and traditional construction expertise, designed to make construction project management more efficient, accurate, and accessible to professionals at all levels.

---

**Built with ❤️ for the construction industry using the latest Android development practices.**
