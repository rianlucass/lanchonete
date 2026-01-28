# Sistema de Controle de Estoque - Documentação Completa

## Visão Geral

Sistema profissional de controle de estoque baseado em **movimentações**, seguindo princípios utilizados em ERPs modernos.

### Princípio Fundamental

**O estoque atual é calculado a partir das movimentações registradas**, não por edição direta de saldo.

Toda alteração no estoque gera um registro de movimentação com:
- Usuário responsável
- Data e hora
- Motivo
- Tipo de movimentação
- Rastreabilidade completa (origem da movimentação)

## Tipos de Movimentação

| Tipo | Descrição | Impacto no Estoque |
|------|-----------|-------------------|
| **ENTRY** | Compra, reposição, devolução recebida | ➕ Aumenta |
| **EXIT** | Venda, consumo, transferência enviada | ➖ Diminui |
| **ADJUSTMENT** | Correção após inventário | ➕➖ Pode aumentar ou diminuir |
| **LOSS** | Avaria, vencimento, extravio | ➖ Diminui |

---

## Endpoints da API

### 1. Registrar Entrada de Estoque

**POST** `/api/stock/entry`

**Descrição:** Registra uma entrada de produtos no estoque (compras, reposições, devoluções).

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 50,
  "reason": "Compra do fornecedor XYZ - Nota Fiscal 12345"
}
```

**Response:** `200 OK`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Coca Cola 2L",
  "movementType": "ENTRY",
  "quantity": 50,
  "dateTime": "2026-01-28T10:30:00",
  "responsibleUser": "admin@lanchonete.com",
  "reason": "Compra do fornecedor XYZ - Nota Fiscal 12345",
  "originType": "MANUAL",
  "originId": null
}
```

**Erros Possíveis:**
- `400 Bad Request` - Dados inválidos ou produto não encontrado
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 2. Registrar Saída de Estoque

**POST** `/api/stock/exit`

**Descrição:** Registra uma saída de produtos do estoque (vendas, consumo, transferências).

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 10,
  "reason": "Venda - Cliente João Silva"
}
```

**Response:** `200 OK`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Coca Cola 2L",
  "movementType": "EXIT",
  "quantity": 10,
  "dateTime": "2026-01-28T14:15:30",
  "responsibleUser": "admin@lanchonete.com",
  "reason": "Venda - Cliente João Silva",
  "originType": "MANUAL",
  "originId": null
}
```

**Erros Possíveis:**
- `400 Bad Request` - Estoque insuficiente ou produto não encontrado
  ```json
  {
    "error": "Insufficient stock. Available: 5, Requested: 10"
  }
  ```
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 3. Registrar Ajuste de Estoque

**POST** `/api/stock/adjustment`

**Descrição:** Ajusta o estoque para um valor específico (inventário, correções). O sistema calcula automaticamente a diferença.

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "targetQuantity": 30,
  "reason": "Ajuste após inventário mensal - Janeiro/2026"
}
```

**Como funciona:**
- Estoque atual: 25 unidades
- Quantidade desejada (targetQuantity): 30 unidades
- Ajuste calculado automaticamente: +5 unidades

**Response:** `200 OK`
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Coca Cola 2L",
  "movementType": "ADJUSTMENT",
  "quantity": 5,
  "dateTime": "2026-01-28T18:00:00",
  "responsibleUser": "admin@lanchonete.com",
  "reason": "Ajuste após inventário mensal - Janeiro/2026",
  "originType": "INVENTORY_ADJUSTMENT",
  "originId": null
}
```

**Erros Possíveis:**
- `400 Bad Request` - Produto não encontrado ou quantidade já está no valor desejado
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 4. Registrar Perda de Estoque

**POST** `/api/stock/loss`

**Descrição:** Registra perdas de produtos (avarias, vencimentos, extravios).

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 5,
  "reason": "Produto vencido - Lote 2024-01-15"
}
```

**Response:** `200 OK`
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440004",
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Coca Cola 2L",
  "movementType": "LOSS",
  "quantity": 5,
  "dateTime": "2026-01-28T16:45:20",
  "responsibleUser": "admin@lanchonete.com",
  "reason": "Produto vencido - Lote 2024-01-15",
  "originType": "MANUAL",
  "originId": null
}
```

**Erros Possíveis:**
- `400 Bad Request` - Estoque insuficiente ou produto não encontrado
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 5. Registrar Movimentação Genérica

**POST** `/api/stock/movement`

**Descrição:** Registra uma movimentação com controle total dos parâmetros (uso avançado).

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "movementType": "ENTRY",
  "quantity": 20,
  "reason": "Recebimento de pedido de compra #789",
  "originType": "PURCHASE_ORDER",
  "originId": "PO-2026-001"
}
```

**Tipos válidos para `movementType`:**
- `ENTRY` - Entrada
- `EXIT` - Saída
- `ADJUSTMENT` - Ajuste
- `LOSS` - Perda

**Response:** `200 OK`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440005",
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Coca Cola 2L",
  "movementType": "ENTRY",
  "quantity": 20,
  "dateTime": "2026-01-28T11:20:15",
  "responsibleUser": "admin@lanchonete.com",
  "reason": "Recebimento de pedido de compra #789",
  "originType": "PURCHASE_ORDER",
  "originId": "PO-2026-001"
}
```

**Erros Possíveis:**
- `400 Bad Request` - Dados inválidos, tipo de movimentação inválido ou estoque insuficiente
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 6. Consultar Estoque Atual de um Produto

**GET** `/api/stock/product/{productId}`

**Descrição:** Retorna o estoque atual calculado e detalhamento completo das movimentações.

**Exemplo de URL:** 
```
GET /api/stock/product/550e8400-e29b-41d4-a716-446655440000
```

**Response:** `200 OK`
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Coca Cola 2L",
  "currentStock": 65,
  "totalEntries": 100,
  "totalExits": 30,
  "totalAdjustments": 0,
  "totalLosses": 5
}
```

**Cálculo do estoque:**
```
currentStock = totalEntries + totalAdjustments - totalExits - totalLosses
currentStock = 100 + 0 - 30 - 5 = 65
```

**Erros Possíveis:**
- `404 Not Found` - Produto não encontrado
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 7. Listar Movimentações de um Produto

**GET** `/api/stock/movements/product/{productId}`

**Descrição:** Retorna histórico completo de todas as movimentações de um produto específico.

**Exemplo de URL:**
```
GET /api/stock/movements/product/550e8400-e29b-41d4-a716-446655440000
```

**Response:** `200 OK`
```json
[
  {
    "id": "aa0e8400-e29b-41d4-a716-446655440005",
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "productName": "Coca Cola 2L",
    "movementType": "ENTRY",
    "quantity": 20,
    "dateTime": "2026-01-28T11:20:15",
    "responsibleUser": "admin@lanchonete.com",
    "reason": "Recebimento de pedido de compra #789",
    "originType": "PURCHASE_ORDER",
    "originId": "PO-2026-001"
  },
  {
    "id": "990e8400-e29b-41d4-a716-446655440004",
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "productName": "Coca Cola 2L",
    "movementType": "LOSS",
    "quantity": 5,
    "dateTime": "2026-01-28T10:30:00",
    "responsibleUser": "admin@lanchonete.com",
    "reason": "Produto vencido - Lote 2024-01-15",
    "originType": "MANUAL",
    "originId": null
  },
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "productName": "Coca Cola 2L",
    "movementType": "EXIT",
    "quantity": 10,
    "dateTime": "2026-01-27T14:15:30",
    "responsibleUser": "vendedor@lanchonete.com",
    "reason": "Venda - Cliente João Silva",
    "originType": "ORDER",
    "originId": "ORD-2026-456"
  }
]
```

**Características:**
- Ordenado por data (mais recente primeiro)
- Histórico completo e imutável
- Rastreabilidade total

**Erros Possíveis:**
- `404 Not Found` - Produto não encontrado
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 8. Listar Todas as Movimentações

**GET** `/api/stock/movements`

**Descrição:** Retorna todas as movimentações do sistema (todos os produtos).

**Response:** `200 OK`
```json
[
  {
    "id": "aa0e8400-e29b-41d4-a716-446655440005",
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "productName": "Coca Cola 2L",
    "movementType": "ENTRY",
    "quantity": 20,
    "dateTime": "2026-01-28T11:20:15",
    "responsibleUser": "admin@lanchonete.com",
    "reason": "Recebimento de pedido de compra #789",
    "originType": "PURCHASE_ORDER",
    "originId": "PO-2026-001"
  },
  {
    "id": "bb0e8400-e29b-41d4-a716-446655440006",
    "productId": "660e8400-e29b-41d4-a716-446655441111",
    "productName": "Hamburguer Especial",
    "movementType": "EXIT",
    "quantity": 5,
    "dateTime": "2026-01-28T11:10:00",
    "responsibleUser": "cozinha@lanchonete.com",
    "reason": "Consumo para produção - Pedido #123",
    "originType": "ORDER",
    "originId": "ORD-2026-123"
  },
  {
    "id": "cc0e8400-e29b-41d4-a716-446655440007",
    "productId": "770e8400-e29b-41d4-a716-446655442222",
    "productName": "Batata Frita",
    "movementType": "ADJUSTMENT",
    "quantity": -3,
    "dateTime": "2026-01-28T10:00:00",
    "responsibleUser": "admin@lanchonete.com",
    "reason": "Ajuste após inventário - divergência encontrada",
    "originType": "INVENTORY_ADJUSTMENT",
    "originId": null
  }
]
```

**Características:**
- Todas as movimentações de todos os produtos
- Ordenado por data (mais recente primeiro)
- Útil para auditoria geral

**Erros Possíveis:**
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

### 9. Listar Movimentações por Tipo

**GET** `/api/stock/movements/type/{type}`

**Descrição:** Retorna todas as movimentações de um tipo específico.

**Tipos válidos:**
- `ENTRY` - Todas as entradas
- `EXIT` - Todas as saídas
- `ADJUSTMENT` - Todos os ajustes
- `LOSS` - Todas as perdas

**Exemplo de URL:**
```
GET /api/stock/movements/type/LOSS
```

**Response:** `200 OK`
```json
[
  {
    "id": "990e8400-e29b-41d4-a716-446655440004",
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "productName": "Coca Cola 2L",
    "movementType": "LOSS",
    "quantity": 5,
    "dateTime": "2026-01-28T16:45:20",
    "responsibleUser": "admin@lanchonete.com",
    "reason": "Produto vencido - Lote 2024-01-15",
    "originType": "MANUAL",
    "originId": null
  },
  {
    "id": "dd0e8400-e29b-41d4-a716-446655440008",
    "productId": "880e8400-e29b-41d4-a716-446655443333",
    "productName": "Refrigerante Guaraná 2L",
    "movementType": "LOSS",
    "quantity": 2,
    "dateTime": "2026-01-27T09:30:00",
    "responsibleUser": "estoquista@lanchonete.com",
    "reason": "Avaria - Embalagem danificada no transporte",
    "originType": "MANUAL",
    "originId": null
  }
]
```

**Uso prático:**
- Analisar todas as perdas do período
- Revisar todos os ajustes feitos
- Acompanhar entradas ou saídas específicas

**Erros Possíveis:**
- `400 Bad Request` - Tipo de movimentação inválido
- `401 Unauthorized` - Usuário não autenticado
- `403 Forbidden` - Usuário sem permissão ADMIN

---

## Exemplos Práticos de Uso

### Exemplo 1: Receber Mercadoria

```bash
# 1. Conferente recebe produtos do fornecedor
# 2. Registra entrada no sistema

curl -X POST http://localhost:8080/api/stock/entry \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 100,
    "reason": "Compra - NF 45678 - Fornecedor Bebidas XYZ Ltda"
  }'
```

### Exemplo 2: Vender Produto

```bash
# Sistema de vendas registra saída automaticamente

curl -X POST http://localhost:8080/api/stock/exit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2,
    "reason": "Venda - Pedido #789 - Cliente: João Silva"
  }'
```

### Exemplo 3: Fazer Inventário

```bash
# 1. Consultar estoque atual
curl -X GET http://localhost:8080/api/stock/product/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer SEU_TOKEN_JWT"

# Resposta: { "currentStock": 47 }
# Contagem física: 50 unidades

# 2. Ajustar para o valor correto
curl -X POST http://localhost:8080/api/stock/adjustment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "targetQuantity": 50,
    "reason": "Inventário mensal Janeiro/2026 - Ajuste de +3 unidades conforme contagem física"
  }'
```

### Exemplo 4: Registrar Perda

```bash
curl -X POST http://localhost:8080/api/stock/loss \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 12,
    "reason": "Produto vencido - Lote 2024-12-15 - Validade expirada em 2026-01-20"
  }'
```

---

## Resumo de Todas as Rotas

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/stock/entry` | Registra entrada de estoque |
| POST | `/api/stock/exit` | Registra saída de estoque |
| POST | `/api/stock/adjustment` | Ajusta estoque para valor específico |
| POST | `/api/stock/loss` | Registra perda de estoque |
| POST | `/api/stock/movement` | Registra movimentação genérica |
| GET | `/api/stock/product/{id}` | Consulta estoque atual de um produto |
| GET | `/api/stock/movements/product/{id}` | Lista movimentações de um produto |
| GET | `/api/stock/movements` | Lista todas as movimentações |
| GET | `/api/stock/movements/type/{type}` | Lista movimentações por tipo |

**Autenticação:** Todas as rotas requerem token JWT e role ADMIN.
