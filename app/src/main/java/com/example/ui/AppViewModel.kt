package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.Comment
import com.example.data.Post
import com.example.data.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    Splash,
    Login,
    Register,
    MainFeed
}

enum class MainTab {
    Feed,       // Facebook-like church feed / portfolio
    CreatePost, // New post / portfolio item creation
    About       // About the Church "AD Missão Boas Novas" 
}

data class PresetImage(val name: String, val url: String, val description: String)

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // Preset High Quality Unsplash Images for Church Activities
    val presetImages = listOf(
        PresetImage(
            name = "Cruz e Luz",
            url = "https://images.unsplash.com/photo-1438032005730-c779502df39b?w=600&auto=format&fit=crop&q=80",
            description = "Símbolo de fé e redenção"
        ),
        PresetImage(
            name = "Bíblia Aberta",
            url = "https://images.unsplash.com/photo-1504052434569-70ad585e515d?w=600&auto=format&fit=crop&q=80",
            description = "Estudo bíblico e palavra de Deus"
        ),
        PresetImage(
            name = "Louvor e Adoração",
            url = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            description = "Ministério de música e louvor"
        ),
        PresetImage(
            name = "Comunhão Social",
            url = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=600&auto=format&fit=crop&q=80",
            description = "Eventos de comunhão e ação social"
        )
    )

    // Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Bottom Tab
    private val _currentTab = MutableStateFlow(MainTab.Feed)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Authenticated User State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Posts State
    val allPosts: StateFlow<List<Post>> = repository.allPosts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Inputs for Auth Screens ---
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")

    val regName = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regPassword = MutableStateFlow("")
    val regConfirmPassword = MutableStateFlow("")
    
    // UI feedback flows
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // --- Inputs for Creating/Editing Posts ---
    val postTitle = MutableStateFlow("")
    val postText = MutableStateFlow("")
    val postSelectedImageUrl = MutableStateFlow(presetImages.first().url)
    val postCategory = MutableStateFlow("Culto") // "Culto", "Evento", "Testemunho", "Aviso"

    // --- Comment selection ---
    private val _selectedPostIdForComments = MutableStateFlow<Long?>(null)
    val selectedPostIdForComments: StateFlow<Long?> = _selectedPostIdForComments.asStateFlow()

    private val _commentsForSelectedPost = MutableStateFlow<List<Comment>>(emptyList())
    val commentsForSelectedPost: StateFlow<List<Comment>> = _commentsForSelectedPost.asStateFlow()

    val newCommentText = MutableStateFlow("")

    init {
        // Prepopulate default items if empty
        viewModelScope.launch {
            val currentList = repository.allPosts.first()
            if (currentList.isEmpty()) {
                prepopulateDatabase()
            }
        }
        
        // Listen to comment requests
        viewModelScope.launch {
            _selectedPostIdForComments.collect { postId ->
                if (postId != null) {
                    repository.getCommentsForPost(postId).collect { comments ->
                        _commentsForSelectedPost.value = comments
                    }
                } else {
                    _commentsForSelectedPost.value = emptyList()
                }
            }
        }

        // Transition from splash screen
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            _currentScreen.value = AppScreen.Login
        }
    }

    private suspend fun prepopulateDatabase() {
        // Default users
        val pastor = User(
            id = 100L,
            name = "Pastor Marcos Oliveira",
            email = "prmarcos@boasnovas.org",
            password = "admin",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80"
        )
        val youthLeader = User(
            id = 101L,
            name = "Diácona Talita Reis",
            email = "talitareis@boasnovas.org",
            password = "jovens",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80"
        )
        
        repository.registerUser(pastor)
        repository.registerUser(youthLeader)

        // Seed inspiring portfolio posts
        repository.createPost(
            Post(
                id = 1L,
                userId = 100L,
                userName = pastor.name,
                userAvatar = pastor.avatarUrl,
                title = "Novo Templo e Conclusão das Obras",
                text = "Louvamos a Deus por mais este marco! A reforma do nosso prédio principal foi finalizada. Um ambiente climatizado e moderno pronto para acolher mais pessoas e expandir o Reino de Deus em nossa comunidade do bairro Missão. Visite AD Missão Boas Novas neste domingo às 19h!",
                imageUrl = presetImages[0].url,
                category = "Evento",
                likesCount = 28,
                commentsCount = 3
            )
        )

        repository.createPost(
            Post(
                id = 2L,
                userId = 101L,
                userName = youthLeader.name,
                userAvatar = youthLeader.avatarUrl,
                title = "Congresso de Jovens 'Conexão Eleitos'",
                text = "Que noite espetacular! No último final de semana tivemos a abertura do nosso Congresso de Jovens. Fomos profundamente impactados pela ministração da Palavra e o louvor ungido. Foram registradas duas decisões por Cristo e imensa renovação espiritual. Confira as fotos!",
                imageUrl = presetImages[2].url,
                category = "Culto",
                likesCount = 42,
                commentsCount = 2
            )
        )

        repository.createPost(
            Post(
                id = 3L,
                userId = 100L,
                userName = pastor.name,
                userAvatar = pastor.avatarUrl,
                title = "Campanha do Kilo - Assistência Boas Novas",
                text = "Nosso ministério de ação social realizou ontem a montagem de mais 40 cestas básicas que serão entregues às famílias cadastradas em vulnerabilidade. Expressamos profunda gratidão a cada irmão que doou com amor e generosidade. Continuamos recebendo alimentos não-perecíveis no templo.",
                imageUrl = presetImages[3].url,
                category = "Testemunho",
                likesCount = 15,
                commentsCount = 1
            )
        )

        // Setup some initial comments
        repository.addComment(Comment(postId = 1L, userName = "Elaine Santos", content = "Ficou lindo demais pastor! Que bênção!"))
        repository.addComment(Comment(postId = 1L, userName = "Felipe Mota", content = "Glória a Deus. Estarei presente no domingo."))
        repository.addComment(Comment(postId = 1L, userName = "Pastor Marcos Oliveira", content = "Deus abençoe a todos os contribuidores."))
        repository.addComment(Comment(postId = 2L, userName = "Elaine Santos", content = "Foi um divisor de águas! Amém!"))
        repository.addComment(Comment(postId = 2L, userName = "Gabriel Silva", content = "Eita glória, que culto tremendo!"))
        repository.addComment(Comment(postId = 3L, userName = "Felipe Mota", content = "Igreja ativa e abençoando vidas!"))
    }

    // --- Authentication Actions ---
    fun onLoginClick() {
        val email = loginEmail.value.trim()
        val password = loginPassword.value

        if (email.isEmpty() || password.isEmpty()) {
            _authError.value = "Preencha todos os campos do formulário!"
            return
        }

        viewModelScope.launch {
            _authError.value = null
            val user = repository.getUserByEmail(email)
            if (user != null && user.password == password) {
                _currentUser.value = user
                _currentScreen.value = AppScreen.MainFeed
                _currentTab.value = MainTab.Feed
                _toastMessage.emit("Bem-vindo(a), ${user.name}!")
                // Clear fields
                loginEmail.value = ""
                loginPassword.value = ""
            } else {
                _authError.value = "E-mail ou senha incorretos!"
            }
        }
    }

    fun onRegisterClick() {
        val name = regName.value.trim()
        val email = regEmail.value.trim()
        val password = regPassword.value
        val confirm = regConfirmPassword.value

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            _authError.value = "Por favor, preencha todos os campos!"
            return
        }

        if (password != confirm) {
            _authError.value = "As senhas preenchidas não coincidem!"
            return
        }

        if (password.length < 4) {
            _authError.value = "A senha deve ter no mínimo 4 caracteres!"
            return
        }

        viewModelScope.launch {
            _authError.value = null
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _authError.value = "Este e-mail já está cadastrado em nossa rede!"
                return@launch
            }

            val newUser = User(
                name = name,
                email = email,
                password = password,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80" // standard avatar
            )
            val newUserId = repository.registerUser(newUser)
            if (newUserId > 0) {
                _toastMessage.emit("Cadastro realizado com sucesso! Faça login.")
                // Automatically switch to Login
                _currentScreen.value = AppScreen.Login
                loginEmail.value = email
                // Clear state
                regName.value = ""
                regEmail.value = ""
                regPassword.value = ""
                regConfirmPassword.value = ""
            } else {
                _authError.value = "Ocorreu um erro ao realizar o cadastro. Tente novamente."
            }
        }
    }

    fun navigateToRegister() {
        _authError.value = null
        _currentScreen.value = AppScreen.Register
    }

    fun navigateToLogin() {
        _authError.value = null
        _currentScreen.value = AppScreen.Login
    }

    fun logout() {
        viewModelScope.launch {
            _currentUser.value = null
            _currentScreen.value = AppScreen.Login
            _toastMessage.emit("Sessão encerrada com sucesso!")
        }
    }

    // --- Tab Navigation ---
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    // --- Feed Operations ---
    fun onLikePost(post: Post) {
        viewModelScope.launch {
            val updated = post.copy(likesCount = post.likesCount + 1)
            repository.updatePost(updated)
            _toastMessage.emit("Você curtiu esta publicação gloriosa!")
        }
    }

    fun onDeletePost(post: Post) {
        viewModelScope.launch {
            if (_currentUser.value?.id == post.userId || _currentUser.value?.email?.endsWith("@boasnovas.org") == true) {
                repository.deletePost(post.id)
                _toastMessage.emit("Publicação removida com sucesso.")
            } else {
                _toastMessage.emit("Apenas o autor ou pastor pode excluir!")
            }
        }
    }

    fun onCreatePostClick() {
        val title = postTitle.value.trim()
        val text = postText.value.trim()
        val imageUrl = postSelectedImageUrl.value
        val user = _currentUser.value ?: return

        if (title.isEmpty() || text.isEmpty()) {
            viewModelScope.launch {
                _toastMessage.emit("Preencha o título e o texto da novidade!")
            }
            return
        }

        viewModelScope.launch {
            val newPost = Post(
                userId = user.id,
                userName = user.name,
                userAvatar = user.avatarUrl,
                title = title,
                text = text,
                imageUrl = imageUrl,
                category = postCategory.value,
                likesCount = 0,
                commentsCount = 0
            )

            val success = repository.createPost(newPost)
            if (success > 0) {
                _toastMessage.emit("Sua publicação foi compartilhada na Missão!")
                // Reset fields
                postTitle.value = ""
                postText.value = ""
                postSelectedImageUrl.value = presetImages.first().url
                postCategory.value = "Culto"
                
                // Go back to the feed
                _currentTab.value = MainTab.Feed
            } else {
                _toastMessage.emit("Erro ao salvar a publicação.")
            }
        }
    }

    // --- Comment Operations ---
    fun selectPostForComments(postId: Long?) {
        _selectedPostIdForComments.value = postId
        newCommentText.value = ""
    }

    fun submitComment() {
        val postId = _selectedPostIdForComments.value ?: return
        val commentContent = newCommentText.value.trim()
        val user = _currentUser.value ?: return

        if (commentContent.isEmpty()) return

        viewModelScope.launch {
            val comment = Comment(
                postId = postId,
                userName = user.name,
                content = commentContent
            )
            repository.addComment(comment)
            
            // Find post and update comments counter
            val activePosts = allPosts.value
            val match = activePosts.find { it.id == postId }
            if (match != null) {
                val updated = match.copy(commentsCount = match.commentsCount + 1)
                repository.updatePost(updated)
            }

            newCommentText.value = ""
            _toastMessage.emit("Comentário adicionado com sucesso!")
        }
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
