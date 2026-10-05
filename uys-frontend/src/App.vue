<template>
  <div class="dashboard-container">
    <!-- Top Bar -->
    <header class="app-header">
      <div class="header-left">
        <div class="brand-badge">✈</div>
        <div>
          <h1 class="brand-title">Flight Operations Control Center</h1>
          <p class="brand-subtitle">Real-Time Dispatch, Scheduling & Event Stream</p>
        </div>
      </div>
      <div class="header-right">
        <!-- User Session & Role Badge -->
        <div class="auth-user-badge">
          <div class="user-meta">
            <span class="user-name">👤 {{ activeUser.username }}</span>
            <el-tag :type="currentRole === 'ROLE_DISPATCHER' ? 'success' : 'warning'" effect="dark" size="small" class="role-tag">
              {{ currentRole === 'ROLE_DISPATCHER' ? 'DISPATCHER (FULL ACCESS)' : 'BI ANALYST (READ ONLY)' }}
            </el-tag>
          </div>
          <el-button size="small" type="primary" plain @click="loginDialogVisible = true" class="switch-user-btn">
            Sign In / Switch Role
          </el-button>
          <el-button v-if="currentToken" size="small" type="danger" plain @click="logout" class="switch-user-btn">
            Sign Out
          </el-button>
        </div>

        <div class="live-clock">
          <span class="clock-dot"></span>
          <span class="clock-time">{{ currentTime }}</span>
        </div>

        <!-- Only Dispatcher can view & use CSV Batch Import -->
        <el-upload
          v-if="currentRole === 'ROLE_DISPATCHER'"
          action="/api/v1/flights/upload-csv"
          name="file"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-button type="primary" plain :icon="Upload">
            Batch Import (CSV)
          </el-button>
        </el-upload>
      </div>
    </header>

    <!-- KPI Metric Cards -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon total">🛫</div>
        <div class="kpi-body">
          <span class="kpi-label">Active Flights</span>
          <span class="kpi-value">{{ uniqueFlightList.length }}</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon scheduled">🕒</div>
        <div class="kpi-body">
          <span class="kpi-label">Scheduled</span>
          <span class="kpi-value">{{ countByStatus('SCHEDULED') }}</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon boarding">🎫</div>
        <div class="kpi-body">
          <span class="kpi-label">Boarding</span>
          <span class="kpi-value warning-text">{{ countByStatus('BOARDING') }}</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon delayed">⚠️</div>
        <div class="kpi-body">
          <span class="kpi-label">Delayed</span>
          <span class="kpi-value danger-text">{{ countByStatus('DELAYED') }}</span>
        </div>
      </div>
    </div>

    <!-- Workspace -->
    <div class="workspace-grid">
      <!-- Left Panel: Manual Entry (Dispatcher) OR Locked Box (Analyst/Guest) -->
      <section class="form-section">
        <el-card shadow="never" class="surface-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">Flight Entry & Dispatch</span>
              <el-tag size="small" :type="currentRole === 'ROLE_DISPATCHER' ? 'info' : 'danger'" effect="plain">
                {{ currentRole === 'ROLE_DISPATCHER' ? 'Manual Entry Active' : 'Locked' }}
              </el-tag>
            </div>
          </template>

          <!-- DISPATCHER: Full Entry Form -->
          <el-form
            v-if="currentRole === 'ROLE_DISPATCHER'"
            ref="flightFormRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="clean-form"
          >
            <div class="form-row">
              <el-form-item label="Airline" prop="airlineCode" class="flex-1">
                <el-select
                  v-model="form.airlineCode"
                  placeholder="Airline"
                  filterable
                  style="width: 100%;"
                  @change="onAirlineChange"
                >
                  <el-option
                    v-for="al in airlines"
                    :key="al.code"
                    :label="`${al.code} - ${al.name}`"
                    :value="al.code"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="Flight Number" prop="flightNumber" class="flex-1">
                <el-input
                  v-model="form.flightNumber"
                  placeholder="e.g. TK1923"
                  maxlength="7"
                  clearable
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="Origin" prop="departureStation" class="flex-1">
                <el-select v-model="form.departureStation" placeholder="Origin" filterable style="width: 100%;">
                  <el-option
                    v-for="st in stations"
                    :key="st.id"
                    :label="`${st.iataCode} - ${st.name}`"
                    :value="st.iataCode"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="Destination" prop="arrivalStation" class="flex-1">
                <el-select v-model="form.arrivalStation" placeholder="Destination" filterable style="width: 100%;">
                  <el-option
                    v-for="st in stations"
                    :key="st.id"
                    :label="`${st.iataCode} - ${st.name}`"
                    :value="st.iataCode"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="Departure (STD)" prop="departureTime" class="flex-1">
                <el-date-picker
                  v-model="form.departureTime"
                  type="datetime"
                  placeholder="STD Time"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%;"
                />
              </el-form-item>

              <el-form-item label="Arrival (STA)" prop="arrivalTime" class="flex-1">
                <el-date-picker
                  v-model="form.arrivalTime"
                  type="datetime"
                  placeholder="STA Time"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%;"
                />
              </el-form-item>
            </div>

            <el-form-item label="Aircraft Type" prop="aircraftType">
              <el-select v-model="form.aircraftType" placeholder="Select Aircraft" filterable style="width: 100%;">
                <el-option
                  v-for="ac in aircraftTypes"
                  :key="ac.code"
                  :label="`${ac.code} (${ac.model})`"
                  :value="ac.code"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="Flight Category">
              <el-radio-group v-model="form.flightType" class="category-radio">
                <el-radio-button value="PASSENGER">Passenger</el-radio-button>
                <el-radio-button value="CARGO">Cargo</el-radio-button>
                <el-radio-button value="POSITION">Position</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <div class="form-actions">
              <el-button @click="resetForm">Clear</el-button>
              <el-button type="primary" :loading="saving" @click="submitForm">
                Save Flight
              </el-button>
            </div>
          </el-form>

          <!-- BI ANALYST / GUEST: Locked Panel -->
          <div v-else class="analyst-lock-box">
            <div class="lock-icon">🔒</div>
            <h3>Read-Only Monitoring Access</h3>
            <p>
              Current session <strong>{{ activeUser.username }}</strong> has read-only privileges.
              Flight creation, deletion, and batch CSV imports are restricted.
            </p>
            <el-button size="small" type="primary" @click="loginDialogVisible = true">
              Switch to Dispatcher Account
            </el-button>
          </div>
        </el-card>
      </section>

      <!-- Right Panel: Live Flights Table -->
      <section class="table-section">
        <el-card shadow="never" class="surface-card">
          <template #header>
            <div class="card-header">
              <div class="header-with-badge">
                <span class="header-title">Live Dispatch Monitor</span>
                <span class="count-pill">{{ filteredFlights.length }} Listed</span>
              </div>
              <div class="table-tools">
                <el-input
                  v-model="searchQuery"
                  placeholder="Search flight, station..."
                  prefix-icon="Search"
                  size="small"
                  clearable
                  style="width: 220px;"
                />
                <el-button :icon="Refresh" circle size="small" @click="fetchFlights" :loading="loading" />
              </div>
            </div>
          </template>

          <el-table
            :data="filteredFlights"
            stripe
            style="width: 100%"
            v-loading="loading"
            empty-text="No flights found"
            height="465"
          >
            <el-table-column prop="flightNumber" label="Flight" width="115" fixed>
              <template #default="{ row }">
                <div class="flight-cell">
                  <span v-if="isFlightImminent(row)" class="pulse-indicator" title="Departure imminent"></span>
                  <span class="flight-code">{{ row.flightNumber }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Route" width="140" align="center">
              <template #default="{ row }">
                <div class="route-badge">
                  <span>{{ row.departureStation }}</span>
                  <span class="arrow">➔</span>
                  <span>{{ row.arrivalStation }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Type" width="110" align="center">
              <template #default="{ row }">
                <span class="type-pill">{{ row.flightType || 'PASSENGER' }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="departureTime" label="Scheduled (STD)" min-width="160">
              <template #default="{ row }">
                <span :class="['time-val', { 'urgent': isFlightImminent(row) }]">
                  {{ formatDateTime(row.departureTime) }}
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="version" label="Ver." width="75" align="center">
              <template #default="{ row }">
                <button class="ver-badge" @click="openAuditHistory(row)">
                  v{{ row.version }}
                </button>
              </template>
            </el-table-column>

            <el-table-column prop="status" label="Status" width="125" align="center">
              <template #default="{ row }">
                <span :class="['status-chip', row.status.toLowerCase()]">
                  {{ row.status }}
                </span>
              </template>
            </el-table-column>

            <!-- ACTION: Only Dispatcher Allowed -->
            <el-table-column label="Action" width="110" align="center" fixed="right">
              <template #default="{ row }">
                <el-dropdown
                  v-if="currentRole === 'ROLE_DISPATCHER'"
                  trigger="click"
                  @command="(cmd: string) => handleAction(row, cmd)"
                >
                  <el-button size="small" text bg class="manage-btn">
                    Manage <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="dark-dropdown">
                      <el-dropdown-item command="BOARDING">BOARDING</el-dropdown-item>
                      <el-dropdown-item command="DELAYED">DELAYED</el-dropdown-item>
                      <el-dropdown-item command="DEPARTED">DEPARTED</el-dropdown-item>
                      <el-dropdown-item command="CANCELLED">CANCELLED</el-dropdown-item>
                      <el-dropdown-item command="DELETE" divided style="color: #ef4444;">
                        Delete Flight
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <span v-else class="read-only-badge">Read Only</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>
    </div>

    <!-- Audit Drawer -->
    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="420px"
      :title="`Flight Audit: ${selectedFlight?.flightNumber || ''}`"
    >
      <div v-if="selectedFlight" class="drawer-inner">
        <div class="drawer-header-card">
          <div class="dh-top">
            <span class="dh-code">{{ selectedFlight.flightNumber }}</span>
            <span :class="['status-chip', selectedFlight.status.toLowerCase()]">
              {{ selectedFlight.status }}
            </span>
          </div>
          <div class="dh-sub">
            <span>Route: <strong>{{ selectedFlight.departureStation }} ➔ {{ selectedFlight.arrivalStation }}</strong></span>
            <span>Version: <strong>v{{ selectedFlight.version }}</strong></span>
          </div>
        </div>

        <div class="timeline-wrap" v-loading="historyLoading">
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in flightHistory"
              :key="index"
              :type="getTimelineType(item.status || item.eventType)"
              :timestamp="formatDateTime(item.eventTime || item.departureTime)"
              placement="top"
            >
              <div class="timeline-box">
                <div class="tb-head">
                  <span class="tb-name">{{ item.eventType || 'STATUS_UPDATE' }}</span>
                  <span class="tb-ver">v{{ item.version || (index + 1) }}</span>
                </div>
                <div class="tb-body">
                  Status: <strong>{{ item.status }}</strong>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>

          <el-empty
            v-if="!historyLoading && flightHistory.length === 0"
            description="No previous audit records."
            :image-size="50"
          />
        </div>
      </div>
    </el-drawer>

    <!-- AUTHENTICATION DIALOG (SIGN IN / SWITCH USER) -->
    <el-dialog
      v-model="loginDialogVisible"
      title="UYS Authentication & Authorization"
      width="380px"
      class="dark-dialog"
      :close-on-click-modal="false"
      @open="resetLoginForm"
    >
      <div class="login-modal-body">
        <p class="login-desc">Sign in with your credentials to access operations:</p>

        <el-form label-position="top">
          <el-form-item label="Username">
            <el-input
              v-model="loginForm.username"
              placeholder="Enter username"
              autocomplete="off"
              clearable
            />
          </el-form-item>
          <el-form-item label="Password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="Enter password"
              autocomplete="new-password"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="loginDialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="loginLoading" @click="performLogin">
            Sign In
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Refresh, Upload, ArrowDown, Search } from '@element-plus/icons-vue'
import { Client } from '@stomp/stompjs'

interface Station {
  id: number
  name: string
  iataCode: string
  city: string
}

interface Airline {
  code: string
  name: string
}

interface AircraftType {
  code: string
  model: string
}

interface Flight {
  id: number
  flightNumber: string
  departureStation: string
  arrivalStation: string
  departureTime: string
  arrivalTime?: string
  flightType?: string
  version: number
  status: string
  isCurrent: boolean
}

interface FlightAuditItem {
  flightNumber: string
  version?: number
  status: string
  eventType?: string
  eventTime?: string
  departureTime?: string
}

// -------------------------------------------------------------
// AUTHENTICATION & JWT STATE
// -------------------------------------------------------------
const loginDialogVisible = ref(false)
const loginLoading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const resetLoginForm = () => {
  loginForm.username = ''
  loginForm.password = ''
}

const currentRole = ref(localStorage.getItem('uys_role') || 'ROLE_BI_ANALYST')
const currentToken = ref(localStorage.getItem('uys_token') || '')
const activeUser = reactive({
  username: localStorage.getItem('uys_username') || 'Guest (Observer)'
})

const uploadHeaders = computed(() => {
  return currentToken.value ? { Authorization: `Bearer ${currentToken.value}` } : {}
})

const performLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('Please enter username and password.')
    return
  }

  loginLoading.value = true
  try {
    const res = await axios.post('/api/v1/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    const { token, username, role } = res.data
    currentToken.value = token
    activeUser.username = username

    const cleanRole = (role || '').toUpperCase()
    if (cleanRole.includes('ADMIN') || cleanRole.includes('DISPATCHER')) {
      currentRole.value = 'ROLE_DISPATCHER'
    } else {
      currentRole.value = 'ROLE_BI_ANALYST'
    }

    localStorage.setItem('uys_token', token)
    localStorage.setItem('uys_username', username)
    localStorage.setItem('uys_role', currentRole.value)

    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

    ElMessage.success(`Successfully signed in as ${username} (${currentRole.value === 'ROLE_DISPATCHER' ? 'Dispatcher' : 'Analyst'})`)
    loginDialogVisible.value = false
    loginForm.password = ''

    await fetchFlights()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Authentication failed! Invalid credentials.')
  } finally {
    loginLoading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('uys_token')
  localStorage.removeItem('uys_username')
  localStorage.removeItem('uys_role')

  currentToken.value = ''
  currentRole.value = 'ROLE_BI_ANALYST'
  activeUser.username = 'Guest (Observer)'
  delete axios.defaults.headers.common['Authorization']

  ElMessage.info('Signed out. Switched to read-only guest mode.')
}

axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('uys_token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// -------------------------------------------------------------
// STATE & FORM DATA
// -------------------------------------------------------------
const flightFormRef = ref<FormInstance>()
const flights = ref<Flight[]>([])
const stations = ref<Station[]>([])
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const currentTime = ref('')

const airlines = ref<Airline[]>([
  { code: 'TK', name: 'Turkish Airlines' },
  { code: 'PC', name: 'Pegasus Airlines' },
  { code: 'XQ', name: 'SunExpress' },
  { code: 'AJ', name: 'AJet' },
  { code: 'LH', name: 'Lufthansa' }
])

const aircraftTypes = ref<AircraftType[]>([
  { code: 'B738', model: 'Boeing 737-800' },
  { code: 'B77W', model: 'Boeing 777-300ER' },
  { code: 'A320', model: 'Airbus A320neo' },
  { code: 'A321', model: 'Airbus A321-200' },
  { code: 'A359', model: 'Airbus A350-900' }
])

const drawerVisible = ref(false)
const selectedFlight = ref<Flight | null>(null)
const flightHistory = ref<FlightAuditItem[]>([])
const historyLoading = ref(false)

const form = reactive({
  airlineCode: 'TK',
  flightNumber: '',
  departureStation: '',
  arrivalStation: '',
  departureTime: '',
  arrivalTime: '',
  aircraftType: 'B738',
  flightType: 'PASSENGER'
})

const isFormDirty = computed(() => {
  return form.flightNumber !== '' || form.departureStation !== '' || form.arrivalStation !== '' || form.departureTime !== ''
})

const rules = reactive<FormRules>({
  airlineCode: [{ required: true, message: 'Select airline', trigger: 'change' }],
  flightNumber: [
    { required: true, message: 'Flight number required', trigger: 'blur' },
    { pattern: /^[A-Z0-9]{3,8}$/, message: 'Valid format: TK1923', trigger: 'blur' }
  ],
  departureStation: [{ required: true, message: 'Origin required', trigger: 'change' }],
  arrivalStation: [{ required: true, message: 'Destination required', trigger: 'change' }],
  departureTime: [{ required: true, message: 'STD required', trigger: 'change' }],
  aircraftType: [{ required: true, message: 'Aircraft required', trigger: 'change' }]
})

const onAirlineChange = (val: string) => {
  if (!form.flightNumber || form.flightNumber.length <= 2) {
    form.flightNumber = val
  }
}

// Mükerrer uçuşları tekilleştirip sadece en güncel olanı alan liste
const uniqueFlightList = computed(() => {
  const currentOnly = flights.value.filter(f => f.isCurrent)
  const map = new Map<string, Flight>()
  for (const item of currentOnly) {
    const existing = map.get(item.flightNumber)
    if (!existing || item.id > existing.id) {
      map.set(item.flightNumber, item)
    }
  }
  return Array.from(map.values())
})

const filteredFlights = computed(() => {
  let list = uniqueFlightList.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(f =>
      f.flightNumber.toLowerCase().includes(q) ||
      f.departureStation.toLowerCase().includes(q) ||
      f.arrivalStation.toLowerCase().includes(q)
    )
  }
  return list.sort((a, b) => new Date(a.departureTime).getTime() - new Date(b.departureTime).getTime())
})

const countByStatus = (status: string) => {
  return uniqueFlightList.value.filter(f => f.status === status).length
}

const isFlightImminent = (flight: Flight) => {
  if (flight.status === 'DEPARTED' || flight.status === 'CANCELLED') return false
  const diffMinutes = (new Date(flight.departureTime).getTime() - new Date().getTime()) / 60000
  return diffMinutes <= 45
}

const fetchStations = async () => {
  try {
    const res = await axios.get<Station[]>('/api/v1/stations')
    stations.value = res.data
  } catch (err: any) {
    ElMessage.warning('Station reference data could not be retrieved.')
  }
}

const fetchFlights = async () => {
  loading.value = true
  try {
    const res = await axios.get<Flight[]>('/api/v1/flights')
    flights.value = res.data
  } catch (err: any) {
    ElMessage.error('Failed to retrieve flights: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

// -------------------------------------------------------------
// WEBSOCKET & STOMP REAL-TIME CLIENT (NATIVE WS)
// -------------------------------------------------------------
let stompClient: Client | null = null

const initWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws-uys/websocket`

  stompClient = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('✅ WebSocket / STOMP bağlantısı başarıyla kuruldu!')
      stompClient?.subscribe('/topic/flights', (message) => {
        if (message.body) {
          console.log('⚡ Canlı uçuş güncellemesi alındı:', JSON.parse(message.body))
          fetchFlights()
        }
      })
    },
    onStompError: (frame) => {
      console.error('❌ STOMP Broker Hatası:', frame.headers['message'])
    }
  })

  stompClient.activate()
}

const submitForm = async () => {
  if (!flightFormRef.value) return

  await flightFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('Please fill in required fields.')
      return
    }

    if (form.departureStation === form.arrivalStation) {
      ElMessage.error('Origin and Destination cannot be identical.')
      return
    }

    if (form.arrivalTime && form.departureTime && new Date(form.arrivalTime) <= new Date(form.departureTime)) {
      ElMessage.error('Arrival (STA) must be after Departure (STD)!')
      return
    }

    saving.value = true
    try {
      await axios.post('/api/v1/flights', {
        flightNumber: form.flightNumber,
        departureStation: form.departureStation,
        arrivalStation: form.arrivalStation,
        departureTime: form.departureTime,
        flightType: form.flightType
      })
      ElMessage.success('Flight saved & dispatched to Kafka.')
      resetForm()
      await fetchFlights()
    } catch (err: any) {
      ElMessage.error(err.response?.data?.message || 'Could not save flight!')
    } finally {
      saving.value = false
    }
  })
}

const handleAction = async (flight: Flight, command: string) => {
  if (command === 'DELETE') {
    try {
      await ElMessageBox.confirm(
        `Are you sure you want to delete flight ${flight.flightNumber}?`,
        'Confirm Deletion',
        { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning' }
      )
      await axios.delete(`/api/v1/flights/${flight.id}`)
      ElMessage.success(`Flight ${flight.flightNumber} deleted.`)
      await fetchFlights()
    } catch (e) {
      // Canceled
    }
  } else {
    try {
      await axios.put(`/api/v1/flights/${flight.flightNumber}`, { status: command })
      ElMessage.success(`Flight ${flight.flightNumber} updated to ${command}`)
      await fetchFlights()
    } catch (err: any) {
      ElMessage.error('Status update failed: ' + (err.response?.data?.message || err.message))
    }
  }
}

const openAuditHistory = async (flight: Flight) => {
  selectedFlight.value = flight
  drawerVisible.value = true
  historyLoading.value = true
  flightHistory.value = []

  try {
    const res = await axios.get(`/api/v1/flights/${flight.flightNumber}/history`)
    flightHistory.value = res.data
  } catch (err) {
    const allVersions = flights.value.filter(f => f.flightNumber === flight.flightNumber)
    if (allVersions.length > 0) {
      flightHistory.value = allVersions.map(v => ({
        flightNumber: v.flightNumber,
        version: v.version,
        status: v.status,
        eventType: v.version === 1 ? 'FLIGHT_CREATED' : 'STATUS_CHANGED',
        departureTime: v.departureTime,
        eventTime: v.departureTime
      }))
    } else {
      flightHistory.value = [
        {
          flightNumber: flight.flightNumber,
          version: flight.version,
          status: flight.status,
          eventType: 'CURRENT_STATE',
          departureTime: flight.departureTime
        }
      ]
    }
  } finally {
    historyLoading.value = false
  }
}

const getTimelineType = (statusOrEvent: string) => {
  if (!statusOrEvent) return 'primary'
  const val = statusOrEvent.toUpperCase()
  if (val.includes('BOARDING')) return 'warning'
  if (val.includes('DELAYED') || val.includes('CANCEL')) return 'danger'
  if (val.includes('DEPARTED')) return 'success'
  return 'primary'
}

const resetForm = () => {
  if (flightFormRef.value) {
    flightFormRef.value.resetFields()
  }
  form.flightType = 'PASSENGER'
  form.airlineCode = 'TK'
  form.aircraftType = 'B738'
}

const handleUploadSuccess = () => {
  ElMessage.success('Batch flights imported successfully via CSV.')
  fetchFlights()
}

const handleUploadError = (err: any) => {
  ElMessage.error('CSV Import Error: ' + (err.message || 'Error occurred'))
}

const formatDateTime = (val?: string) => {
  if (!val) return '-'
  return val.replace('T', ' ').slice(0, 16)
}

const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toTimeString().split(' ')[0] + ' UTC+3'
}

let clockInterval: any = null

const handleGlobalKeydown = (e: KeyboardEvent) => {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 's') {
    e.preventDefault()
    if (currentRole.value === 'ROLE_DISPATCHER') {
      submitForm()
    }
  }
}

const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (isFormDirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(() => {
  const savedToken = localStorage.getItem('uys_token')
  if (savedToken) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
  }

  fetchStations()
  fetchFlights()
  initWebSocket()
  updateClock()
  clockInterval = setInterval(updateClock, 1000)
  window.addEventListener('keydown', handleGlobalKeydown)
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  if (clockInterval) clearInterval(clockInterval)
  if (stompClient) stompClient.deactivate()
  window.removeEventListener('keydown', handleGlobalKeydown)
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style>
/* Global Reset & Dark Theme */
html, body {
  margin: 0 !important;
  padding: 0 !important;
  background-color: #0b0f19 !important;
  color: #e2e8f0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.el-popper.is-light {
  background: #131b2e !important;
  border: 1px solid #1e293b !important;
}

.el-dropdown-menu {
  background: #131b2e !important;
  border: none !important;
  padding: 6px !important;
}

.el-dropdown-menu__item {
  color: #cbd5e1 !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  border-radius: 4px;
}

.el-dropdown-menu__item:hover {
  background-color: #1e293b !important;
  color: #38bdf8 !important;
}

/* Table */
.el-table {
  --el-table-bg-color: #131b2e !important;
  --el-table-tr-bg-color: #131b2e !important;
  --el-table-header-bg-color: #0f172a !important;
  --el-table-border-color: #1e293b !important;
  --el-table-text-color: #cbd5e1 !important;
  --el-table-header-text-color: #94a3b8 !important;
  --el-table-row-hover-bg-color: #182238 !important;
  font-size: 13px !important;
}

.el-table th.el-table__cell {
  font-weight: 600 !important;
  font-size: 11px !important;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: #101726 !important;
}

/* Inputs */
.el-input__wrapper,
.el-select__wrapper {
  background-color: #0f172a !important;
  box-shadow: 0 0 0 1px #334155 inset !important;
  border-radius: 6px !important;
}

.el-input__inner {
  color: #f1f5f9 !important;
  font-size: 13px !important;
}

.el-form-item {
  margin-bottom: 14px !important;
}

.el-form-item__label {
  color: #94a3b8 !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  padding-bottom: 4px !important;
}

.el-radio-button__inner {
  background-color: #0f172a !important;
  border-color: #334155 !important;
  color: #94a3b8 !important;
  font-size: 12px !important;
}

.el-radio-button__original-radio:checked + .el-radio-button__inner {
  background-color: #2563eb !important;
  border-color: #2563eb !important;
  color: #ffffff !important;
}

/* Drawer */
.el-drawer {
  background-color: #0f172a !important;
  color: #e2e8f0 !important;
  border-left: 1px solid #1e293b !important;
}

.el-drawer__header {
  margin-bottom: 12px !important;
  color: #f8fafc !important;
  font-weight: 700 !important;
  border-bottom: 1px solid #1e293b;
  padding: 16px 20px !important;
}

/* Dark Dialog */
.dark-dialog .el-dialog {
  background-color: #131b2e !important;
  border: 1px solid #1e293b !important;
  border-radius: 10px !important;
}

.dark-dialog .el-dialog__title {
  color: #f8fafc !important;
  font-weight: 700;
  font-size: 16px;
}
</style>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: #0b0f19;
  color: #e2e8f0;
  padding: 24px 32px;
}

/* Header */
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-badge {
  background: #2563eb;
  color: white;
  font-size: 22px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #ffffff;
}

.brand-subtitle {
  font-size: 12px;
  color: #64748b;
  margin: 2px 0 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* Auth Badge */
.auth-user-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #131b2e;
  padding: 4px 12px;
  border-radius: 8px;
  border: 1px solid #1e293b;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 12px;
  font-weight: 600;
  color: #cbd5e1;
  font-family: monospace;
}

.role-tag {
  font-weight: 700 !important;
  font-size: 10px !important;
}

.switch-user-btn {
  font-size: 11px !important;
}

.live-clock {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #131b2e;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid #1e293b;
  font-family: monospace;
  font-size: 13px;
  color: #38bdf8;
}

.clock-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  box-shadow: 0 0 6px #22c55e;
}

/* KPI Cards */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.kpi-card {
  background: #131b2e;
  border: 1px solid #1e293b;
  border-radius: 10px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.kpi-icon {
  font-size: 20px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #0f172a;
}

.kpi-body {
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.warning-text { color: #fbbf24; }
.danger-text { color: #f87171; }

/* Workspace */
.workspace-grid {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 20px;
  align-items: start;
}

@media (max-width: 1100px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }
}

.surface-card {
  background: #131b2e !important;
  border: 1px solid #1e293b !important;
  border-radius: 10px !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-weight: 600;
  font-size: 14px;
  color: #f1f5f9;
}

.header-with-badge {
  display: flex;
  align-items: center;
  gap: 10px;
}

.count-pill {
  font-size: 11px;
  background: #0f172a;
  color: #94a3b8;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
  border: 1px solid #1e293b;
}

.table-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clean-form {
  padding-top: 2px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.flex-1 {
  flex: 1;
}

.category-radio {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

/* Locked Box */
.analyst-lock-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;
  background: #0f172a;
  border: 1px dashed #334155;
  border-radius: 8px;
}

.lock-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.analyst-lock-box h3 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #f8fafc;
}

.analyst-lock-box p {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
  margin: 0 0 16px 0;
}

/* Table Cells */
.flight-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flight-code {
  font-weight: 700;
  font-family: monospace;
  font-size: 13px;
  color: #38bdf8;
}

.route-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #0f172a;
  padding: 3px 8px;
  border-radius: 5px;
  border: 1px solid #1e293b;
  font-weight: 700;
  font-size: 11px;
  color: #f8fafc;
}

.arrow {
  color: #64748b;
  font-size: 10px;
}

.type-pill {
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  background: #0f172a;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #1e293b;
}

.time-val {
  font-family: monospace;
  font-size: 12px;
  color: #cbd5e1;
}

.time-val.urgent {
  color: #fbbf24 !important;
  font-weight: 700;
}

.ver-badge {
  background: #0f172a;
  border: 1px solid #334155;
  color: #38bdf8;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.ver-badge:hover {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

.pulse-indicator {
  width: 7px;
  height: 7px;
  background-color: #ef4444;
  border-radius: 50%;
  animation: pulse-red 1.5s infinite;
}

@keyframes pulse-red {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7); }
  70% { box-shadow: 0 0 0 5px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

.status-chip {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  padding: 3px 8px;
  border-radius: 5px;
  text-transform: uppercase;
}

.status-chip.scheduled {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.status-chip.boarding {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.status-chip.delayed {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.status-chip.departed {
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-chip.cancelled {
  background: rgba(100, 116, 139, 0.2);
  color: #94a3b8;
  border: 1px solid rgba(100, 116, 139, 0.3);
}

.manage-btn {
  font-size: 11px !important;
}

.read-only-badge {
  font-size: 11px;
  color: #64748b;
  background: #0f172a;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #1e293b;
}

/* Drawer */
.drawer-inner {
  padding: 0 4px;
}

.drawer-header-card {
  background: #131b2e;
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 20px;
}

.dh-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dh-code {
  font-size: 18px;
  font-weight: 700;
  color: #38bdf8;
  font-family: monospace;
}

.dh-sub {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
}

.timeline-wrap {
  padding-top: 4px;
}

.timeline-box {
  background: #131b2e;
  border: 1px solid #1e293b;
  border-radius: 6px;
  padding: 10px 12px;
}

.tb-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.tb-name {
  font-size: 12px;
  font-weight: 700;
  color: #f1f5f9;
}

.tb-ver {
  font-size: 10px;
  background: #0f172a;
  color: #38bdf8;
  padding: 1px 6px;
  border-radius: 3px;
  font-family: monospace;
}

.tb-body {
  font-size: 12px;
  color: #94a3b8;
}

/* Modal Stilleri */
.login-modal-body {
  padding: 8px 0;
}

.login-desc {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 14px;
}
</style>